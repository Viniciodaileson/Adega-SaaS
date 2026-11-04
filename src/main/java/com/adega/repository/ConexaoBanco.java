package com.adega.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class ConexaoBanco {

    private static final String URL_PADRAO = "jdbc:sqlite:adega.db";

    private ConexaoBanco() {
    }

    public static Connection abrir() throws SQLException {
        String url = System.getProperty("adega.db.url", URL_PADRAO);
        return DriverManager.getConnection(url);
    }

    public static void inicializar() throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS bebidas (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL COLLATE NOCASE UNIQUE,
                    preco TEXT NOT NULL,
                    quant_estoque INTEGER NOT NULL CHECK (quant_estoque >= 0)
                )
                """;

        try (Connection conexao = abrir();
             Statement comando = conexao.createStatement()) {

            comando.execute(sql);
        }
    }
}
