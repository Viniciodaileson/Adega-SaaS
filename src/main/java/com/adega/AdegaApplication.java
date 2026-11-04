package com.adega;

import com.adega.repository.ConexaoBanco;
import com.adega.ui.CadastroBebidas;

import java.sql.SQLException;

public class AdegaApplication {

    public static void main(String[] args) {

        try {
            ConexaoBanco.inicializar();
        } catch (SQLException e) {
            System.out.println("Não foi possível abrir o banco de dados: " + e.getMessage());
            return;
        }

        new CadastroBebidas().executar();
    }
}
