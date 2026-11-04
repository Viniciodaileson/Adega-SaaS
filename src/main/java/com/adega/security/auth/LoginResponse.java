package com.adega.security.auth;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {
}
