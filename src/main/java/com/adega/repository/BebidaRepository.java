package com.adega.repository;

import com.adega.model.Bebida;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BebidaRepository {

    public Bebida salvar(Bebida bebida) {

        String sql = "INSERT INTO bebidas (nome, preco, quant_estoque) VALUES (?, ?, ?)";

        try (Connection conexao = ConexaoBanco.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            comando.setString(1, bebida.getNome());
            comando.setString(2, bebida.getPreco().toPlainString());
            comando.setInt(3, bebida.getQuantEstoque());

            comando.executeUpdate();

            try (ResultSet chaves = comando.getGeneratedKeys()) {

                if (chaves.next()) {
                    int idGerado = chaves.getInt(1);

                    return new Bebida(
                            idGerado,
                            bebida.getNome(),
                            bebida.getPreco(),
                            bebida.getQuantEstoque()
                    );
                }
            }

            throw new IllegalStateException("O banco não devolveu o id da bebida cadastrada.");

        } catch (SQLException e) {
            throw new IllegalStateException(traduzirErro(e), e);
        }
    }

    public List<Bebida> listar() {

        String sql = "SELECT id, nome, preco, quant_estoque FROM bebidas ORDER BY nome";

        List<Bebida> bebidas = new ArrayList<>();

        try (Connection conexao = ConexaoBanco.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql);
             ResultSet resultado = comando.executeQuery()) {

            while (resultado.next()) {
                bebidas.add(lerBebida(resultado));
            }

        } catch (SQLException e) {
            throw new IllegalStateException(traduzirErro(e), e);
        }

        return bebidas;
    }

    public Bebida buscarPorNome(String nome) {

        String sql = "SELECT id, nome, preco, quant_estoque FROM bebidas WHERE nome = ?";

        try (Connection conexao = ConexaoBanco.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setString(1, nome);

            try (ResultSet resultado = comando.executeQuery()) {

                if (resultado.next()) {
                    return lerBebida(resultado);
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(traduzirErro(e), e);
        }

        return null;
    }

    public void atualizarEstoque(Bebida bebida) {

        String sql = "UPDATE bebidas SET quant_estoque = ? WHERE id = ?";

        try (Connection conexao = ConexaoBanco.abrir();
             PreparedStatement comando = conexao.prepareStatement(sql)) {

            comando.setInt(1, bebida.getQuantEstoque());
            comando.setInt(2, bebida.getId());

            if (comando.executeUpdate() != 1) {
                throw new IllegalStateException("Nenhuma bebida encontrada com o id " + bebida.getId() + ".");
            }

        } catch (SQLException e) {
            throw new IllegalStateException(traduzirErro(e), e);
        }
    }

    private Bebida lerBebida(ResultSet resultado) throws SQLException {

        return new Bebida(
                resultado.getInt("id"),
                resultado.getString("nome"),
                new BigDecimal(resultado.getString("preco")),
                resultado.getInt("quant_estoque")
        );
    }

    private String traduzirErro(SQLException e) {

        if (e.getMessage() != null && e.getMessage().contains("UNIQUE constraint failed")) {
            return "Já existe uma bebida cadastrada com esse nome.";
        }

        return "Erro ao acessar o banco de dados: " + e.getMessage();
    }
}
