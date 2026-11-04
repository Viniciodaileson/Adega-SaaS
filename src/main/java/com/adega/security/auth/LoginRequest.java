package com.adega.security.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "O login é obrigatório.")
        @Size(max = 60, message = "O login deve ter no máximo 60 caracteres.")
        String login,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(max = 200, message = "A senha deve ter no máximo 200 caracteres.")
        String senha
) {
}
