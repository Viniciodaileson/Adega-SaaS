public record Resultado(boolean sucesso, String mensagem) {

    public static Resultado ok(String mensagem) {
        return new Resultado(true, mensagem);
    }

    public static Resultado falha(String mensagem) {
        return new Resultado(false, mensagem);
    }
}
