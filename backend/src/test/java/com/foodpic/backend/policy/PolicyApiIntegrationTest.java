package com.foodpic.backend.policy;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 정책 회귀 전용 API 통합 테스트.
 *
 * <p>의도:
 * - 정책이 서비스/리포지토리 레이어를 우회해도 API 레벨에서 항상 적용되는지 검증
 * - 실제 PostgreSQL 트랜잭션 정합성을 확인
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PolicyApiIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("foodpic_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void override(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    @DisplayName("R1: Block 관계면 post/comment/profile 조회가 API 레벨에서 차단된다")
    void r1_blockRelation_blocksPostCommentAndProfileRead() {
        String blockerToken = issueToken("user-blocker");
        String blockedToken = issueToken("user-blocked");

        long blockerId = createUser("blocker");
        long blockedId = createUser("blocked");
        long postId = createPost(blockedToken, "blocked-user-post");

        createBlock(blockerToken, blockedId);

        givenAuth(blockerToken)
                .when().get("/api/feed/posts/{postId}", postId)
                .then().statusCode(403);

        givenAuth(blockerToken)
                .when().get("/api/feed/posts/{postId}/comments", postId)
                .then().statusCode(403);

        givenAuth(blockerToken)
                .when().get("/api/profiles/{userId}", blockedId)
                .then().statusCode(403);

        assertThat(blockerId).isPositive();
    }

    @Test
    @DisplayName("R2: like 연속 POST는 멱등하며 count=1, row 1개를 유지한다")
    void r2_likeIsIdempotent() {
        String userToken = issueToken("user-like");
        String ownerToken = issueToken("user-owner");
        long postId = createPost(ownerToken, "post-for-like");

        givenAuth(userToken)
                .contentType(ContentType.JSON)
                .when().post("/api/feed/posts/{postId}/likes", postId)
                .then().statusCode(200)
                .body("count", org.hamcrest.Matchers.equalTo(1));

        givenAuth(userToken)
                .contentType(ContentType.JSON)
                .when().post("/api/feed/posts/{postId}/likes", postId)
                .then().statusCode(200)
                .body("count", org.hamcrest.Matchers.equalTo(1));

        Integer likeRows = jdbcTemplate.queryForObject(
                "select count(*) from feed_post_like where post_id = ?",
                Integer.class,
                postId
        );
        assertThat(likeRows).isEqualTo(1);
    }

    @Test
    @DisplayName("R3: comment 삭제는 soft delete 처리되고 FeedPost.commentCount가 감소한다")
    void r3_commentDelete_isSoftDeleteAndDecrementsCommentCount() {
        String userToken = issueToken("user-comment");
        String ownerToken = issueToken("user-owner2");
        long postId = createPost(ownerToken, "post-for-comment");

        long commentId = createComment(userToken, postId, "hello");

        Integer before = jdbcTemplate.queryForObject(
                "select comment_count from feed_post where id = ?",
                Integer.class,
                postId
        );
        assertThat(before).isEqualTo(1);

        givenAuth(userToken)
                .when().delete("/api/feed/comments/{commentId}", commentId)
                .then().statusCode(204);

        Boolean deleted = jdbcTemplate.queryForObject(
                "select deleted from feed_comment where id = ?",
                Boolean.class,
                commentId
        );
        Integer after = jdbcTemplate.queryForObject(
                "select comment_count from feed_post where id = ?",
                Integer.class,
                postId
        );

        assertThat(deleted).isTrue();
        assertThat(after).isZero();
    }

    @Test
    @DisplayName("R4: (createdAt,id) cursor 페이지네이션에 중복/누락이 없다")
    void r4_cursorPagination_hasNoDuplicateOrMissingItems() {
        String ownerToken = issueToken("user-page-owner");

        for (int i = 0; i < 25; i++) {
            createPostAt(ownerToken, "post-" + i, Instant.now().minusSeconds(i));
        }

        List<Integer> collectedIds = new java.util.ArrayList<>();
        String cursor = null;

        while (true) {
            Response response = givenAuth(ownerToken)
                    .queryParam("size", 7)
                    .queryParam("cursor", cursor)
                    .when().get("/api/feed/posts")
                    .then().statusCode(200)
                    .extract().response();

            List<Integer> ids = response.jsonPath().getList("items.id", Integer.class);
            if (ids == null || ids.isEmpty()) {
                break;
            }
            collectedIds.addAll(ids);
            cursor = response.jsonPath().getString("nextCursor");
            if (cursor == null || cursor.isBlank()) {
                break;
            }
        }

        assertThat(collectedIds).doesNotHaveDuplicates();
        assertThat(collectedIds).hasSize(25);
    }

    @Test
    @DisplayName("R5: MealEntry 수정 후 기존 FeedPost의 mealSnapshotJson은 불변이다")
    void r5_mealEntryUpdate_doesNotMutateExistingFeedSnapshot() {
        String userToken = issueToken("user-meal");

        long mealEntryId = createMealEntry(userToken, "닭가슴살", 300);
        long postId = createPostFromMealEntry(userToken, mealEntryId);

        String snapshotBefore = jdbcTemplate.queryForObject(
                "select meal_snapshot_json from feed_post where id = ?",
                String.class,
                postId
        );

        updateMealEntry(userToken, mealEntryId, "연어", 550);

        String snapshotAfter = jdbcTemplate.queryForObject(
                "select meal_snapshot_json from feed_post where id = ?",
                String.class,
                postId
        );

        assertThat(snapshotBefore).isEqualTo(snapshotAfter);
    }

    private io.restassured.specification.RequestSpecification givenAuth(String token) {
        return given().header("Authorization", "Bearer " + token);
    }

    private String issueToken(String subject) {
        return subject + "-token";
    }

    private long createUser(String username) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);

        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().post("/api/test-support/users")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private void createBlock(String blockerToken, long blockedUserId) {
        givenAuth(blockerToken)
                .contentType(ContentType.JSON)
                .body(Map.of("blockedUserId", blockedUserId))
                .when().post("/api/blocks")
                .then().statusCode(201);
    }

    private long createPost(String token, String content) {
        return givenAuth(token)
                .contentType(ContentType.JSON)
                .body(Map.of("content", content))
                .when().post("/api/feed/posts")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private void createPostAt(String token, String content, Instant createdAt) {
        givenAuth(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "content", content,
                        "createdAt", createdAt.toString()
                ))
                .when().post("/api/test-support/feed/posts")
                .then().statusCode(201);
    }

    private long createComment(String token, long postId, String text) {
        return givenAuth(token)
                .contentType(ContentType.JSON)
                .body(Map.of("content", text))
                .when().post("/api/feed/posts/{postId}/comments", postId)
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createMealEntry(String token, String mealName, int calories) {
        return givenAuth(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "mealName", mealName,
                        "calories", calories
                ))
                .when().post("/api/meals/entries")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private long createPostFromMealEntry(String token, long mealEntryId) {
        return givenAuth(token)
                .contentType(ContentType.JSON)
                .body(Map.of("mealEntryId", mealEntryId))
                .when().post("/api/feed/posts/from-meal-entry")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    private void updateMealEntry(String token, long mealEntryId, String mealName, int calories) {
        givenAuth(token)
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "mealName", mealName,
                        "calories", calories
                ))
                .when().put("/api/meals/entries/{id}", mealEntryId)
                .then().statusCode(200);
    }
}
