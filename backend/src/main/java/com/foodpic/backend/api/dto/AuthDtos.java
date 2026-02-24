package com.foodpic.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record LoginRequest(@NotBlank String provider, @NotBlank String accessToken) {}
    public record LoginResponse(String userId, String refreshToken, String tokenType) {}
    public record TokenRequest(@NotBlank String refreshToken) {}
    public record TokenResponse(String accessToken, long expiresIn, String tokenType) {}
}
