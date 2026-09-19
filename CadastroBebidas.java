import java.util.Scanner;

public class CadastroBebidas {

    public static void main(String[] args) {

        Bebida heineken = new Bebida("Heineken", 5.00, 20);

        Scanner sc = new Scanner(System.in);

        System.out.println("Digite o nome da bebida: ");
        String nome = sc.nextLine();

        System.out.println("Digite o preço da bebida: ");
        double preco = sc.nextDouble();

        System.out.println("Qual a quantidade de bebidas deseja guardar? ");
        int quantEstoque = sc.nextInt();

        System.out.println("Bebida cadastrada : " + nome + " -- R$ " + preco);
        System.out.println("A quantidade em estoque: " + quantEstoque);

        if (quantEstoque > 0) {
            System.out.println("Produto disponível para venda");
        } else {
            System.out.println("Produto não está disponível!");
        }

        if (heineken.quantEstoque == 0) {
            System.out.println("Produto esgotado!");

        } else if (heineken.quantEstoque > 0) {

            System.out.println("Deseja comprar uma " + nome + " ? ");

            String desejaComprar = sc.next();

            if (heineken.quantEstoque > 0 &&
                    desejaComprar.equalsIgnoreCase("sim")) {

                System.out.println("Bebida comprada com sucesso!");

                heineken.vender();

                System.out.print(
                        "Quantidade em estoque é: " + heineken.quantEstoque);

            } else {
                System.out.print("Cliente não deseja comprar");
            }
        }

        sc.close();

        }
    }
