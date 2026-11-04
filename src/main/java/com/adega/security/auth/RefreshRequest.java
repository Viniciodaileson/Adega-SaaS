package com.adega.security.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(

        @NotBlank(message = "O refresh token é obrigatório.")
        String refreshToken
) {
}
