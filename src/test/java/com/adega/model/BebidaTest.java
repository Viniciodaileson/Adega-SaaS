package com.adega.model;

import com.adega.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BebidaTest {

    @Test
    void normalizaPrecoParaDuasCasas() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("25.999"), 10);

        assertEquals(new BigDecimal("26.00"), bebida.getPreco());
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraNegocioException.class,
                () -> new Bebida("  ", new BigDecimal("10.00"), 1));
    }

    @Test
    void rejeitaPrecoZeroOuNegativo() {
        assertThrows(RegraNegocioException.class,
                () -> new Bebida("Vinho", BigDecimal.ZERO, 1));

        assertThrows(RegraNegocioException.class,
                () -> new Bebida("Vinho", new BigDecimal("-1.00"), 1));
    }

    @Test
    void rejeitaEstoqueNegativo() {
        assertThrows(RegraNegocioException.class,
                () -> new Bebida("Vinho", new BigDecimal("10.00"), -1));
    }

    @Test
    void venderReduzEstoqueECalculaTotal() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("10.00"), 5);

        BigDecimal total = bebida.vender(2);

        assertEquals(new BigDecimal("20.00"), total);
        assertEquals(3, bebida.getQuantEstoque());
    }

    @Test
    void venderAcimaDoEstoqueFalha() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("10.00"), 5);

        assertThrows(RegraNegocioException.class, () -> bebida.vender(6));
        assertEquals(5, bebida.getQuantEstoque());
    }

    @Test
    void venderQuantidadeInvalidaFalha() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("10.00"), 5);

        assertThrows(RegraNegocioException.class, () -> bebida.vender(0));
        assertThrows(RegraNegocioException.class, () -> bebida.vender(-1));
        assertEquals(5, bebida.getQuantEstoque());
    }

    @Test
    void reporAumentaEstoque() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("10.00"), 5);

        bebida.reporEstoque(3);

        assertEquals(8, bebida.getQuantEstoque());
    }

    @Test
    void reporQuantidadeInvalidaFalha() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("10.00"), 5);

        assertThrows(RegraNegocioException.class, () -> bebida.reporEstoque(0));
        assertEquals(5, bebida.getQuantEstoque());
    }

    @Test
    void calculaValorEmEstoque() {
        Bebida bebida = new Bebida("Vinho", new BigDecimal("12.50"), 4);

        assertEquals(new BigDecimal("50.00"), bebida.valorEmEstoque());
    }

    @Test
    void removeEspacosDoNome() {
        Bebida bebida = new Bebida("  Vinho Tinto  ", new BigDecimal("10.00"), 1);

        assertEquals("Vinho Tinto", bebida.getNome());
    }
}
