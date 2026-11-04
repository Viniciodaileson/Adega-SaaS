package com.adega.service;

import com.adega.model.Bebida;
import com.adega.model.Resultado;
import com.adega.repository.ConexaoBanco;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BebidaServiceTest {

    private BebidaService servico;

    @BeforeAll
    static void configurarBanco() {
        System.setProperty("adega.db.url", "jdbc:sqlite:target/adega-test.db");
    }

    @BeforeEach
    void limparBanco() throws SQLException {
        ConexaoBanco.inicializar();

        try (Connection conexao = ConexaoBanco.abrir();
             Statement comando = conexao.createStatement()) {

            comando.execute("DELETE FROM bebidas");
            comando.execute("DELETE FROM sqlite_sequence WHERE name = 'bebidas'");
        }

        servico = new BebidaService();
    }

    @Test
    void cadastraEBuscaBebida() {
        Resultado resultado = servico.cadastrar("Vinho", new BigDecimal("25.90"), 10);

        assertTrue(resultado.sucesso());

        Bebida bebida = servico.buscarPorNome("vinho");

        assertNotNull(bebida);
        assertEquals(1, bebida.getId());
        assertEquals(10, bebida.getQuantEstoque());
    }

    @Test
    void bloqueiaNomeDuplicadoIgnorandoCaixa() {
        servico.cadastrar("Vinho", new BigDecimal("25.90"), 10);

        Resultado resultado = servico.cadastrar("vinho", new BigDecimal("30.00"), 5);

        assertFalse(resultado.sucesso());
        assertEquals(1, servico.listar().size());
    }

    @Test
    void vendaAtualizaEstoqueNoBanco() {
        servico.cadastrar("Vinho", new BigDecimal("25.90"), 10);

        Resultado resultado = servico.vender("Vinho", 3);

        assertTrue(resultado.sucesso());
        assertEquals(new BigDecimal("77.70"), resultado.valorTotal());
        assertEquals(7, servico.buscarPorNome("Vinho").getQuantEstoque());
    }

    @Test
    void vendaSemEstoqueNaoAlteraBanco() {
        servico.cadastrar("Vinho", new BigDecimal("25.90"), 2);

        Resultado resultado = servico.vender("Vinho", 3);

        assertFalse(resultado.sucesso());
        assertEquals(2, servico.buscarPorNome("Vinho").getQuantEstoque());
    }

    @Test
    void reporAtualizaEstoqueNoBanco() {
        servico.cadastrar("Vinho", new BigDecimal("25.90"), 2);

        Resultado resultado = servico.repor("Vinho", 5);

        assertTrue(resultado.sucesso());
        assertEquals(7, servico.buscarPorNome("Vinho").getQuantEstoque());
    }

    @Test
    void vendaDeBebidaInexistenteFalha() {
        Resultado resultado = servico.vender("Skol", 1);

        assertFalse(resultado.sucesso());
    }

    @Test
    void listarRetornaVazioQuandoNaoHaBebidas() {
        assertTrue(servico.listar().isEmpty());
    }
}
