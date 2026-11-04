import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Responsável por abrir a conexão com o SQLite e criar a tabela.
 * O driver (lib/sqlite-jdbc-*.jar) se registra sozinho via ServiceLoader.
 */
public final class ConexaoBanco {

    private static final String URL = "jdbc:sqlite:adega.db";

    private ConexaoBanco() {
    }

    public static Connection abrir() throws SQLException {
        return DriverManager.getConnection(URL);
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
