package com.adega.model;

import java.math.BigDecimal;

public record Resultado(boolean sucesso, String mensagem, BigDecimal valorTotal) {

    public static Resultado ok(String mensagem) {
        return new Resultado(true, mensagem, BigDecimal.ZERO);
    }

    public static Resultado ok(String mensagem, BigDecimal valorTotal) {
        return new Resultado(true, mensagem, valorTotal);
    }

    public static Resultado falha(String mensagem) {
        return new Resultado(false, mensagem, BigDecimal.ZERO);
    }
}
