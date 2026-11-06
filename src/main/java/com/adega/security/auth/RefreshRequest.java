package com.adega.security.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RefreshRequest(

        @NotBlank(message = "O refresh token é obrigatório.")
        @Size(max = 200, message = "O refresh token é inválido.")
        String refreshToken
) {
}
