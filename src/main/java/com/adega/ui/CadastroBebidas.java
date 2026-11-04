package com.adega.ui;

import com.adega.model.Bebida;
import com.adega.model.Resultado;
import com.adega.service.BebidaService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class CadastroBebidas {

    private final Scanner sc = new Scanner(System.in);
    private final BebidaService servico = new BebidaService();

    public void executar() {

        int opcao;

        do {

            exibirMenu();

            opcao = lerInteiro("Digite a opção desejada: ");

            try {

                switch (opcao) {

                    case 1:
                        cadastrarBebida();
                        break;

                    case 2:
                        consultarEstoque();
                        break;

                    case 3:
                        reporEstoque();
                        break;

                    case 4:
                        realizarVenda();
                        break;

                    case 0:
                        System.out.println("Saindo do sistema...");
                        break;

                    default:
                        System.out.println("Opção inválida. Tente novamente.");
                }

            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
            }

        } while (opcao != 0);

        sc.close();
    }

    private void exibirMenu() {

        System.out.println("\n=============== ADEGA ===============");
        System.out.println("1 - Cadastrar bebida");
        System.out.println("2 - Consultar estoque");
        System.out.println("3 - Repor estoque");
        System.out.println("4 - Realizar venda");
        System.out.println("0 - Sair");
        System.out.println("======================================");
    }

    private void cadastrarBebida() {

        System.out.println("\n--- CADASTRO DE BEBIDA ---");

        String nome = lerTexto("Digite o nome da bebida: ");

        if (servico.buscarPorNome(nome) != null) {
            System.out.println("Já existe uma bebida cadastrada com esse nome.");
            return;
        }

        BigDecimal preco = lerDecimal("Digite o preço da bebida: ");
        int quantEstoque = lerInteiro("Digite a quantidade em estoque: ");

        try {

            Resultado resultado = servico.cadastrar(nome, preco, quantEstoque);

            System.out.println(resultado.mensagem());

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível cadastrar: " + e.getMessage());
        }
    }

    private void consultarEstoque() {

        System.out.println("\n--- ESTOQUE ---");

        List<Bebida> bebidas = servico.listar();

        if (bebidas.isEmpty()) {
            System.out.println("Nenhuma bebida cadastrada.");
            return;
        }

        BigDecimal valorTotal = BigDecimal.ZERO;

        for (Bebida bebida : bebidas) {

            System.out.println("----------------------------");
            System.out.println("ID: " + bebida.getId());
            System.out.println("Bebida: " + bebida.getNome());
            System.out.printf("Preço: R$ %.2f%n", bebida.getPreco());
            System.out.println("Quantidade: " + bebida.getQuantEstoque());

            valorTotal = valorTotal.add(bebida.valorEmEstoque());
        }

        System.out.println("----------------------------");
        System.out.printf("Valor total em estoque: R$ %.2f%n", valorTotal);
    }

    private void reporEstoque() {

        System.out.println("\n--- REPOSIÇÃO ---");

        String nome = lerTexto("Digite o nome da bebida: ");

        if (servico.buscarPorNome(nome) == null) {
            System.out.println("Bebida não encontrada: " + nome);
            return;
        }

        int quantidade = lerInteiro("Digite a quantidade a ser reposta: ");

        Resultado resultado = servico.repor(nome, quantidade);

        System.out.println(resultado.mensagem());
        System.out.println("Estoque atual: " + servico.buscarPorNome(nome).getQuantEstoque());
    }

    private void realizarVenda() {

        System.out.println("\n--- VENDA ---");

        String nome = lerTexto("Digite o nome da bebida: ");

        if (servico.buscarPorNome(nome) == null) {
            System.out.println("Bebida não encontrada: " + nome);
            return;
        }

        int quantidade = lerInteiro("Quantas unidades deseja comprar? ");

        Resultado resultado = servico.vender(nome, quantidade);

        System.out.println(resultado.mensagem());

        if (resultado.sucesso()) {
            System.out.printf("Total da venda: R$ %.2f%n", resultado.valorTotal());
        }

        System.out.println("Estoque atual: " + servico.buscarPorNome(nome).getQuantEstoque());
    }

    private String lerTexto(String mensagem) {

        System.out.print(mensagem);

        return sc.nextLine().trim();
    }

    private int lerInteiro(String mensagem) {

        while (true) {

            String entrada = lerTexto(mensagem);

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private BigDecimal lerDecimal(String mensagem) {

        while (true) {

            String entrada = lerTexto(mensagem).replace(",", ".");

            try {
                return new BigDecimal(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número. Ex: 12,50");
            }
        }
    }
}
