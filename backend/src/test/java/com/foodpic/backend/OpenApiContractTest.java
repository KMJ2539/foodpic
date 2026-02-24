package com.foodpic.backend;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractTest {
    @Autowired MockMvc mockMvc;
    ObjectMapper jsonMapper = new ObjectMapper();
    Map<String, Object> openapi;

    @BeforeEach
    void loadSpec() throws Exception {
        try (InputStream is = new ClassPathResource("static/openapi/openapi.yaml").getInputStream()) {
            openapi = new ObjectMapper(new YAMLFactory()).readValue(is, new TypeReference<>() {});
        }
    }

    @Test
    void authAndDomainResponsesMatchOpenApiSchemas() throws Exception {
        JsonNode login = jsonMapper.readTree(mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"provider\":\"kakao\",\"accessToken\":\"abc\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        JsonNode token = jsonMapper.readTree(mockMvc.perform(post("/v1/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + login.get("refreshToken").asText() + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());

        assertMatches("/v1/auth/login", "post", login);
        assertMatches("/v1/auth/token", "post", token);
        assertMatches("/v1/social/profile", "get", callGet("/v1/social/profile"));
        assertMatches("/v1/feed", "get", callGet("/v1/feed"));
        assertMatches("/v1/posts/{postId}", "get", callGet("/v1/posts/post-1"));
        assertMatches("/v1/likes/{postId}", "get", callGet("/v1/likes/post-1"));
        assertMatches("/v1/comments/{commentId}", "get", callGet("/v1/comments/comment-1"));
        assertMatches("/v1/meals/{mealId}", "get", callGet("/v1/meals/meal-1"));
        assertMatches("/v1/photos/{photoId}", "get", callGet("/v1/photos/photo-1"));
        assertMatches("/v1/vision/{photoId}", "get", callGet("/v1/vision/photo-1"));
    }

    private JsonNode callGet(String uri) throws Exception {
        String body = mockMvc.perform(get(uri).header("X-Test-User", "contract-user"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body);
    }

    @SuppressWarnings("unchecked")
    private void assertMatches(String path, String method, JsonNode actual) {
        Map<String, Object> paths = (Map<String, Object>) openapi.get("paths");
        Map<String, Object> operation = (Map<String, Object>) ((Map<String, Object>) paths.get(path)).get(method);
        Map<String, Object> response200 = (Map<String, Object>) ((Map<String, Object>) operation.get("responses")).get("200");
        Map<String, Object> content = (Map<String, Object>) ((Map<String, Object>) response200.get("content")).get("application/json");
        Map<String, Object> schema = resolveSchema((Map<String, Object>) content.get("schema"));
        validateObject(schema, actual);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveSchema(Map<String, Object> schema) {
        if (schema.containsKey("$ref")) {
            String ref = String.valueOf(schema.get("$ref"));
            String schemaName = ref.substring(ref.lastIndexOf('/') + 1);
            Map<String, Object> components = (Map<String, Object>) openapi.get("components");
            Map<String, Object> schemas = (Map<String, Object>) components.get("schemas");
            return (Map<String, Object>) schemas.get(schemaName);
        }
        return schema;
    }

    @SuppressWarnings("unchecked")
    private void validateObject(Map<String, Object> schema, JsonNode actual) {
        Map<String, Object> properties = (Map<String, Object>) schema.getOrDefault("properties", new LinkedHashMap<>());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String field = entry.getKey();
            Map<String, Object> prop = resolveSchema((Map<String, Object>) entry.getValue());
            assertThat(actual.has(field)).isTrue();
            JsonNode node = actual.get(field);
            String type = (String) prop.get("type");
            if (type == null && prop.containsKey("properties")) {
                type = "object";
            }
            if ("string".equals(type)) assertThat(node.isTextual() || node.isNull()).isTrue();
            if ("integer".equals(type)) assertThat(node.isIntegralNumber()).isTrue();
            if ("number".equals(type)) assertThat(node.isNumber()).isTrue();
            if ("boolean".equals(type)) assertThat(node.isBoolean()).isTrue();
            if ("array".equals(type)) {
                assertThat(node.isArray()).isTrue();
                Map<String, Object> itemSchema = resolveSchema((Map<String, Object>) prop.get("items"));
                if (node.isArray() && node.size() > 0) {
                    String itemType = (String) itemSchema.get("type");
                    if ("string".equals(itemType)) assertThat(node.get(0).isTextual()).isTrue();
                    if ("object".equals(itemType) || itemSchema.containsKey("properties")) validateObject(itemSchema, node.get(0));
                }
            }
            if ("object".equals(type)) validateObject(prop, node);
        }
    }
}
