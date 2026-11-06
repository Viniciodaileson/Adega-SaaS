package com.adega.model;

import com.adega.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "bebidas")
public class Bebida {

    private static final int TAMANHO_MAXIMO_NOME = 100;
    private static final int ESTOQUE_MAXIMO = 1_000_000_000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "quant_estoque", nullable = false)
    private int quantEstoque;

    protected Bebida() {
    }

    public Bebida(String nome, BigDecimal preco, int quantEstoque) {
        setNome(nome);
        setPreco(preco);
        setQuantEstoque(quantEstoque);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {

        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("O nome da bebida não pode ser vazio.");
        }

        String normalizado = nome.trim();

        if (normalizado.length() > TAMANHO_MAXIMO_NOME) {
            throw new RegraNegocioException(
                    "O nome da bebida deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres.");
        }

        this.nome = normalizado;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {

        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("O preço deve ser maior que zero.");
        }

        this.preco = preco.setScale(2, RoundingMode.HALF_UP);
    }

    public int getQuantEstoque() {
        return quantEstoque;
    }

    public void setQuantEstoque(int quantEstoque) {

        if (quantEstoque < 0) {
            throw new RegraNegocioException("O estoque não pode ser negativo.");
        }

        if (quantEstoque > ESTOQUE_MAXIMO) {
            throw new RegraNegocioException(
                    "O estoque deve ser no máximo " + ESTOQUE_MAXIMO + " unidades.");
        }

        this.quantEstoque = quantEstoque;
    }

    public BigDecimal vender(int quantidade) {

        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade inválida para venda.");
        }

        if (quantidade > quantEstoque) {
            throw new RegraNegocioException("Quantidade insuficiente em estoque. Disponível: " + quantEstoque);
        }

        quantEstoque -= quantidade;

        return preco.multiply(BigDecimal.valueOf(quantidade));
    }

    public void reporEstoque(int quantidade) {

        if (quantidade <= 0) {
            throw new RegraNegocioException("Quantidade inválida para reposição.");
        }

        long novoEstoque = (long) quantEstoque + quantidade;

        if (novoEstoque > ESTOQUE_MAXIMO) {
            throw new RegraNegocioException(
                    "A reposição ultrapassa o limite de " + ESTOQUE_MAXIMO + " unidades em estoque.");
        }

        quantEstoque = (int) novoEstoque;
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

        return id != null && id.equals(outra.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
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
