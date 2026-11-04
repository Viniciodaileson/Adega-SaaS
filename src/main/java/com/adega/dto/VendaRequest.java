package com.adega.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VendaRequest(

        @NotNull(message = "A quantidade vendida é obrigatória.")
        @Min(value = 1, message = "A quantidade vendida deve ser maior que zero.")
        Integer quantidade
) {
}
