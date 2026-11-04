import java.util.ArrayList;
import java.util.Scanner;

public class CadastroBebidas {

    private static final Scanner SC = new Scanner(System.in);
    private static final ArrayList<Bebida> BEBIDAS = new ArrayList<>();

    public static void main(String[] args) {

        int opcao;

        do {

            exibirMenu();

            opcao = lerInteiro("Digite a opção desejada: ");

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

        } while (opcao != 0);

        SC.close();
    }

    private static void exibirMenu() {

        System.out.println("\n=============== ADEGA ===============");
        System.out.println("1 - Cadastrar bebida");
        System.out.println("2 - Consultar estoque");
        System.out.println("3 - Repor estoque");
        System.out.println("4 - Realizar venda");
        System.out.println("0 - Sair");
        System.out.println("======================================");
    }

    private static void cadastrarBebida() {

        System.out.println("\n--- CADASTRO DE BEBIDA ---");

        String nome = lerTexto("Digite o nome da bebida: ");

        if (buscarBebida(nome) != null) {
            System.out.println("Já existe uma bebida cadastrada com esse nome.");
            return;
        }

        double preco = lerDecimal("Digite o preço da bebida: ");
        int quantEstoque = lerInteiro("Digite a quantidade em estoque: ");

        try {

            Bebida bebida = new Bebida(nome, preco, quantEstoque);

            BEBIDAS.add(bebida);

            System.out.println("Bebida cadastrada com sucesso!");

        } catch (IllegalArgumentException e) {
            System.out.println("Não foi possível cadastrar: " + e.getMessage());
        }
    }

    private static void consultarEstoque() {

        System.out.println("\n--- ESTOQUE ---");

        if (BEBIDAS.isEmpty()) {
            System.out.println("Nenhuma bebida cadastrada.");
            return;
        }

        double valorTotal = 0;

        for (Bebida bebida : BEBIDAS) {

            System.out.println("----------------------------");
            System.out.println("Bebida: " + bebida.getNome());
            System.out.printf("Preço: R$ %.2f%n", bebida.getPreco());
            System.out.println("Quantidade: " + bebida.getQuantEstoque());

            valorTotal += bebida.valorEmEstoque();
        }

        System.out.println("----------------------------");
        System.out.printf("Valor total em estoque: R$ %.2f%n", valorTotal);
    }

    private static void reporEstoque() {

        System.out.println("\n--- REPOSIÇÃO ---");

        if (BEBIDAS.isEmpty()) {
            System.out.println("Nenhuma bebida cadastrada.");
            return;
        }

        String nome = lerTexto("Digite o nome da bebida: ");

        Bebida bebida = buscarBebida(nome);

        if (bebida == null) {
            System.out.println("Bebida não encontrada: " + nome);
            return;
        }

        int quantidade = lerInteiro("Digite a quantidade a ser reposta: ");

        bebida.reporEstoque(quantidade);

        System.out.println("Estoque atual: " + bebida.getQuantEstoque());
    }

    private static void realizarVenda() {

        System.out.println("\n--- VENDA ---");

        if (BEBIDAS.isEmpty()) {
            System.out.println("Nenhuma bebida cadastrada.");
            return;
        }

        String nome = lerTexto("Digite o nome da bebida: ");

        Bebida bebida = buscarBebida(nome);

        if (bebida == null) {
            System.out.println("Bebida não encontrada: " + nome);
            return;
        }

        int quantidade = lerInteiro("Quantas unidades deseja comprar? ");

        boolean vendeu = bebida.vender(quantidade);

        if (vendeu) {
            System.out.printf("Total da venda: R$ %.2f%n", quantidade * bebida.getPreco());
        }

        System.out.println("Estoque atual: " + bebida.getQuantEstoque());
    }

    private static Bebida buscarBebida(String nome) {

        for (Bebida bebida : BEBIDAS) {

            if (bebida.getNome().equalsIgnoreCase(nome)) {
                return bebida;
            }
        }

        return null;
    }

    private static String lerTexto(String mensagem) {

        System.out.print(mensagem);

        return SC.nextLine().trim();
    }

    private static int lerInteiro(String mensagem) {

        while (true) {

            String entrada = lerTexto(mensagem);

            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número inteiro.");
            }
        }
    }

    private static double lerDecimal(String mensagem) {

        while (true) {

            String entrada = lerTexto(mensagem).replace(",", ".");

            try {
                return Double.parseDouble(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido. Digite um número. Ex: 12,50");
            }
        }
    }
}
