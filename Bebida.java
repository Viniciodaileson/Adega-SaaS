public class Bebida {


    String nome;
    double preco;
    int quantEstoque;
    int vender;


    public Bebida(String nome, double preco, int quantEstoque ) {


        this.nome = nome;
        this.preco = preco;
        this.quantEstoque = quantEstoque;
           
    }
 public void vender(){
        quantEstoque = quantEstoque -1;
    }
   
}


