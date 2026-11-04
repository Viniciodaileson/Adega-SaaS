package com.adega.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BebidaTest {

    @Test
    void normalizaPrecoParaDuasCasas() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("25.999"), 10);

        assertEquals(new BigDecimal("26.00"), bebida.getPreco());
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bebida(1, "  ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejeitaPrecoZeroOuNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bebida(1, "Vinho", BigDecimal.ZERO, 1));

        assertThrows(IllegalArgumentException.class,
                () -> new Bebida(1, "Vinho", new BigDecimal("-1.00"), 1));
    }

    @Test
    void rejeitaEstoqueNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bebida(1, "Vinho", new BigDecimal("10.00"), -1));
    }

    @Test
    void rejeitaIdNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> new Bebida(-1, "Vinho", new BigDecimal("10.00"), 1));
    }

    @Test
    void venderReduzEstoque() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("10.00"), 5);

        Resultado resultado = bebida.vender(2);

        assertTrue(resultado.sucesso());
        assertEquals(3, bebida.getQuantEstoque());
    }

    @Test
    void venderAcimaDoEstoqueFalha() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("10.00"), 5);

        Resultado resultado = bebida.vender(6);

        assertFalse(resultado.sucesso());
        assertEquals(5, bebida.getQuantEstoque());
    }

    @Test
    void venderQuantidadeInvalidaFalha() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("10.00"), 5);

        assertFalse(bebida.vender(0).sucesso());
        assertFalse(bebida.vender(-1).sucesso());
        assertEquals(5, bebida.getQuantEstoque());
    }

    @Test
    void avisaQuandoEstoqueZera() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("10.00"), 2);

        Resultado resultado = bebida.vender(2);

        assertTrue(resultado.mensagem().contains("esgotada"));
    }

    @Test
    void reporAumentaEstoque() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("10.00"), 5);

        Resultado resultado = bebida.reporEstoque(3);

        assertTrue(resultado.sucesso());
        assertEquals(8, bebida.getQuantEstoque());
    }

    @Test
    void reporQuantidadeInvalidaFalha() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("10.00"), 5);

        assertFalse(bebida.reporEstoque(0).sucesso());
        assertEquals(5, bebida.getQuantEstoque());
    }

    @Test
    void calculaValorEmEstoque() {
        Bebida bebida = new Bebida(1, "Vinho", new BigDecimal("12.50"), 4);

        assertEquals(new BigDecimal("50.00"), bebida.valorEmEstoque());
    }

    @Test
    void comparaBebidasPeloId() {
        Bebida primeira = new Bebida(1, "Vinho", new BigDecimal("10.00"), 5);
        Bebida segunda = new Bebida(1, "Cerveja", new BigDecimal("8.00"), 2);
        Bebida terceira = new Bebida(2, "Vinho", new BigDecimal("10.00"), 5);

        assertEquals(primeira, segunda);
        assertNotEquals(primeira, terceira);
    }
}
