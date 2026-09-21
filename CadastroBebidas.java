import java.util.Scanner;

public class CadastroBebidas {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        Bebida bebida = null;

        int opcao;

        do {

            System.out.println("\n=============== ADEGA ===============");
            System.out.println("1 - Cadastrar bebida");
            System.out.println("2 - Consultar estoque");
            System.out.println("3 - Repor estoque");
            System.out.println("4 - Realizar venda");
            System.out.println("0 - Sair");
            System.out.println("======================================");
            System.out.print("Digite a opção desejada: ");

            opcao = sc.nextInt();

            switch (opcao) {

                case 1:

                    System.out.println("\n--- CADASTRO DE BEBIDA ---");

                    sc.nextLine();

                    System.out.print("Digite o nome da bebida: ");
                    String nome = sc.nextLine();

                    System.out.print("Digite o preço da bebida: ");
                    double preco = sc.nextDouble();

                    System.out.print("Digite a quantidade em estoque: ");
                    int quantEstoque = sc.nextInt();

                    bebida = new Bebida(nome, preco, quantEstoque);

                    System.out.println("Bebida cadastrada com sucesso!");

                    break;

                case 2:

                    if (bebida == null) {
                        System.out.println("Nenhuma bebida cadastrada.");
                        break;
                    }

                    System.out.println("\n--- ESTOQUE ---");
                    System.out.println("Bebida: " + bebida.nome);
                    System.out.println("Preço: R$ " + bebida.preco);
                    System.out.println("Quantidade: " + bebida.quantEstoque);

                    break;

                case 3:

                    if (bebida == null) {
                        System.out.println("Nenhuma bebida cadastrada.");
                        break;
                    }

                    System.out.println("\n--- REPOSIÇÃO ---");
                    System.out.print("Digite a quantidade a ser reposta: ");

                    int quantidadeReposicao = sc.nextInt();

                    bebida.reporEstoque(quantidadeReposicao);

                    System.out.println(
                        "Estoque atualizado: " + bebida.quantEstoque
                    );

                    break;

                case 4:

                    if (bebida == null) {
                        System.out.println("Nenhuma bebida cadastrada.");
                        break;
                    }

                    System.out.println("\n--- VENDA ---");
                    System.out.print("Quantas unidades deseja comprar? ");

                    int quantidadeVenda = sc.nextInt();

                    bebida.vender(quantidadeVenda);

                    System.out.println(
                        "Estoque atual: " + bebida.quantEstoque
                    );

                    break;

                case 0:

                    System.out.println("Saindo do sistema...");

                    break;

                default:

                    System.out.println("Opção inválida. Tente novamente.");
            }

        } while (opcao != 0);

        sc.close();
    }
}