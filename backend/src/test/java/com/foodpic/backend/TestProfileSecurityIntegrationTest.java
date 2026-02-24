package com.foodpic.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TestProfileSecurityIntegrationTest {
    @Autowired MockMvc mockMvc;

    @Test
    void injectsUserFromHeader() throws Exception {
        mockMvc.perform(get("/v1/social/profile").header("X-Test-User", "qa-user"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMissingTestUserHeaderForProtectedEndpoint() throws Exception {
        mockMvc.perform(get("/v1/social/profile"))
                .andExpect(status().isUnauthorized());
    }
}
