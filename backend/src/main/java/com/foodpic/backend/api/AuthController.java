package com.foodpic.backend.api;

import com.foodpic.backend.api.dto.AuthDtos.LoginRequest;
import com.foodpic.backend.api.dto.AuthDtos.LoginResponse;
import com.foodpic.backend.api.dto.AuthDtos.TokenRequest;
import com.foodpic.backend.api.dto.AuthDtos.TokenResponse;
import com.foodpic.backend.security.JwtTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final JwtTokenService tokenService;

    public AuthController(JwtTokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        String userId = request.provider() + "-user";
        String refreshToken = tokenService.generateToken(userId, 60L * 60L * 24L * 7L);
        return new LoginResponse(userId, refreshToken, "Bearer");
    }

    @PostMapping("/token")
    public TokenResponse token(@Valid @RequestBody TokenRequest request) {
        String userId = tokenService.validateAndGetSubject(request.refreshToken());
        if (userId == null) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        String accessToken = tokenService.generateToken(userId, 3600);
        return new TokenResponse(accessToken, 3600, "Bearer");
    }
}
