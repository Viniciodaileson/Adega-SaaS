public class Bebida {

    private String nome;
    private double preco;
    private int quantEstoque;

    public Bebida(String nome, double preco, int quantEstoque) {
        setNome(nome);
        setPreco(preco);
        setQuantEstoque(quantEstoque);
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

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {

        if (preco <= 0) {
            throw new IllegalArgumentException("O preço deve ser maior que zero.");
        }

        this.preco = preco;
    }

    public int getQuantEstoque() {
        return quantEstoque;
    }

    public void setQuantEstoque(int quantEstoque) {

        if (quantEstoque < 0) {
            throw new IllegalArgumentException("O estoque não pode ser negativo.");
        }

        this.quantEstoque = quantEstoque;
    }

    public boolean vender(int quantidade) {

        if (quantidade <= 0) {
            System.out.println("Quantidade inválida para venda.");
            return false;
        }

        if (quantidade > quantEstoque) {
            System.out.println("Quantidade insuficiente em estoque. Disponível: " + quantEstoque);
            return false;
        }

        quantEstoque -= quantidade;

        System.out.println("Venda realizada com sucesso! Quantidade vendida: " + quantidade);

        if (quantEstoque == 0) {
            System.out.println("A bebida " + nome + " está esgotada.");
        }

        return true;
    }

    public boolean reporEstoque(int quantidade) {

        if (quantidade <= 0) {
            System.out.println("Quantidade inválida para reposição.");
            return false;
        }

        quantEstoque += quantidade;

        System.out.println("Estoque reposto com sucesso! Quantidade adicionada: " + quantidade);

        return true;
    }

    public double valorEmEstoque() {
        return preco * quantEstoque;
    }

    @Override
    public String toString() {
        return String.format(
                "%s | R$ %.2f | %d un. | total: R$ %.2f",
                nome,
                preco,
                quantEstoque,
                valorEmEstoque()
        );
    }
}
