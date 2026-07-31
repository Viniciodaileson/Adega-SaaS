import java.util.Scanner;   

public class CadastroBebidas {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Digite o nome da bebida: ");
        String nome = sc.nextLine();

        System.out.println("Digite o preço da bebida: ");
        double preco = sc.nextDouble();

        System.out.println("Qual a quantidade de bebidas deseja guardar? ");
        int quantEstoque = sc.nextInt();

        System.out.println("Bebida cadastrada : "+ nome + " -- R$ " + preco);
        System.out.println("A quantidade em estoque: "+quantEstoque);
        
        if (quantEstoque > 0){
            System.out.println("Produto disponivel para venda");
        }else{
            System.out.println("Produto Não esta disponivel! ");
        } 

        if (quantEstoque == 0){
            System.out.println("Produto esgostado! ");
        }
        else if (quantEstoque > 0){
        System.out.println("Deseja comprar uma " +nome+ " ? ");
           String desejaComprar = sc.next(); 
        if (quantEstoque > 0 && desejaComprar.equalsIgnoreCase("sim") ){
            System.out.println("Bebida comprada com sucesso! ");
            quantEstoque = quantEstoque - 1;
            System.out.print("Quantidade em estoque é: "+quantEstoque);
        }else {
            System.out.print("Cliente não deseja comprar");    
        }   
      
        sc.close();

        }
    }
}
    

