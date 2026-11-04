package com.adega.dto;

import com.adega.model.Bebida;

import java.math.BigDecimal;

public record BebidaResponse(
        Long id,
        String nome,
        BigDecimal preco,
        int quantEstoque,
        BigDecimal valorEmEstoque
) {

    public static BebidaResponse de(Bebida bebida) {
        return new BebidaResponse(
                bebida.getId(),
                bebida.getNome(),
                bebida.getPreco(),
                bebida.getQuantEstoque(),
                bebida.valorEmEstoque()
        );
    }
}
