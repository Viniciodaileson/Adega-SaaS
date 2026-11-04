import java.math.BigDecimal;
import java.math.RoundingMode;

public class Bebida {

    private final int id;
    private String nome;
    private BigDecimal preco;
    private int quantEstoque;

    /**
     * id 0 significa "ainda não salvo no banco".
     * Depois do INSERT, o repositório devolve a bebida com o id gerado.
     */
    public Bebida(int id, String nome, BigDecimal preco, int quantEstoque) {

        if (id < 0) {
            throw new IllegalArgumentException("O id não pode ser negativo.");
        }

        this.id = id;

        setNome(nome);
        setPreco(preco);
        setQuantEstoque(quantEstoque);
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("O nome da bebida não pode ser vazio.");
        }

        this.nome = nome.trim();
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {

        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }

        // Normaliza para duas casas decimais (ex: 25.9 -> 25.90)
        this.preco = preco.setScale(2, RoundingMode.HALF_UP);
    }

    public int getQuantEstoque() {
        return quantEstoque;
    }

    // Privado de propósito: o estoque só muda por venda ou reposição.
    private void setQuantEstoque(int quantEstoque) {

        if (quantEstoque < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo.");
        }

        this.quantEstoque = quantEstoque;
    }

    public Resultado vender(int quantidade) {

        if (quantidade <= 0) {
            return Resultado.falha("Quantidade inválida para venda.");
        }

        if (quantidade > quantEstoque) {
            return Resultado.falha("Quantidade insuficiente em estoque. Disponível: " + quantEstoque);
        }

        quantEstoque -= quantidade;

        if (quantEstoque == 0) {
            return Resultado.ok(
                    "Venda realizada com sucesso! Quantidade vendida: " + quantidade
                            + ". A bebida " + nome + " está esgotada."
            );
        }

        return Resultado.ok("Venda realizada com sucesso! Quantidade vendida: " + quantidade);
    }

    public Resultado reporEstoque(int quantidade) {

        if (quantidade <= 0) {
            return Resultado.falha("Quantidade inválida para reposição.");
        }

        quantEstoque += quantidade;

        return Resultado.ok("Estoque reposto com sucesso! Quantidade adicionada: " + quantidade);
    }

    public BigDecimal valorEmEstoque() {
        return preco.multiply(BigDecimal.valueOf(quantEstoque));
    }

    @Override
    public boolean equals(Object outro) {

        if (this == outro) {
            return true;
        }

        if (!(outro instanceof Bebida outra)) {
            return false;
        }

        return id == outra.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format(
                "%d - %s | R$ %.2f | %d un. | total: R$ %.2f",
                id,
                nome,
                preco,
                quantEstoque,
                valorEmEstoque()
        );
    }
}
