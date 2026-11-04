package com.adega.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReposicaoRequest(

        @NotNull(message = "A quantidade reposta é obrigatória.")
        @Min(value = 1, message = "A quantidade reposta deve ser maior que zero.")
        Integer quantidade
) {
}
