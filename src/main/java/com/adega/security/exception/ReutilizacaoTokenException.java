package com.adega.security.exception;

public class ReutilizacaoTokenException extends TokenInvalidoException {

    public ReutilizacaoTokenException(String mensagem) {
        super(mensagem);
    }
}
