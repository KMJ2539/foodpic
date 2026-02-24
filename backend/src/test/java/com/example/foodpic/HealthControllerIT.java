package com.example.foodpic;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class HealthControllerIT {

    @Value("${local.server.port}")
    int port;

    @Test
    void healthEndpointReturnsOk() {
        given()
                .port(port)
        .when()
                .get("/api/health")
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("status", equalTo("ok"));
    }
}
