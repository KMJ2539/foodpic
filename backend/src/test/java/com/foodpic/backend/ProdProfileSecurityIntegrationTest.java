package com.foodpic.backend;

import com.foodpic.backend.security.JwtTokenService;
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
@ActiveProfiles("prod")
class ProdProfileSecurityIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired JwtTokenService jwtTokenService;

    @Test
    void requiresBearerToken() throws Exception {
        mockMvc.perform(get("/v1/social/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsValidBearerToken() throws Exception {
        String token = jwtTokenService.generateToken("prod-user", 3600);
        mockMvc.perform(get("/v1/social/profile").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
