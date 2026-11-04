package com.adega.dto;

import java.util.List;

public record ErroResponse(
        int status,
        String erro,
        List<String> detalhes
) {
}
