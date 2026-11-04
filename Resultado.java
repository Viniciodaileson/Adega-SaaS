/**
 * Resultado de uma operação do modelo.
 * Permite que a regra de negócio informe o que aconteceu
 * sem imprimir nada (quem imprime é a interface).
 */
public record Resultado(boolean sucesso, String mensagem) {

    public static Resultado ok(String mensagem) {
        return new Resultado(true, mensagem);
    }

    public static Resultado falha(String mensagem) {
        return new Resultado(false, mensagem);
    }
}
