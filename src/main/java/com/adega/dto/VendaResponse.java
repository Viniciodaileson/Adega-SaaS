package com.adega.dto;

import com.adega.model.Bebida;

import java.math.BigDecimal;

public record VendaResponse(
        Long id,
        String nome,
        int quantidadeVendida,
        BigDecimal valorTotal,
        int quantEstoque
) {

    public static VendaResponse de(Bebida bebida, int quantidadeVendida, BigDecimal valorTotal) {
        return new VendaResponse(
                bebida.getId(),
                bebida.getNome(),
                quantidadeVendida,
                valorTotal,
                bebida.getQuantEstoque()
        );
    }
}
