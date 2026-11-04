package com.adega.api;

import com.adega.dto.ErroResponse;
import com.adega.exception.ConflitoException;
import com.adega.exception.RecursoNaoEncontradoException;
import com.adega.exception.RegraNegocioException;
import com.adega.security.exception.AcessoNegadoException;
import com.adega.security.exception.CredenciaisInvalidasException;
import com.adega.security.exception.TokenInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return resposta(HttpStatus.NOT_FOUND, "Recurso não encontrado", excecao.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> tratarConflito(ConflitoException excecao) {
        return resposta(HttpStatus.CONFLICT, "Conflito", excecao.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException excecao) {
        return resposta(HttpStatus.BAD_REQUEST, "Regra de negócio violada", excecao.getMessage());
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<ErroResponse> tratarTokenInvalido(TokenInvalidoException excecao) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", excecao.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException excecao) {
        return resposta(HttpStatus.UNAUTHORIZED, "Não autenticado", excecao.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> tratarAcessoNegado(AcessoNegadoException excecao) {
        return resposta(HttpStatus.FORBIDDEN, "Acesso negado", excecao.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException excecao) {

        List<String> detalhes = excecao.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();

        return ResponseEntity.badRequest()
                .body(new ErroResponse(HttpStatus.BAD_REQUEST.value(), "Dados inválidos", detalhes));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> tratarCorpoInvalido(HttpMessageNotReadableException excecao) {
        return resposta(HttpStatus.BAD_REQUEST, "Dados inválidos", "O corpo da requisição está inválido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception excecao) {
        return resposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", "Não foi possível processar a requisição.");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String erro, String detalhe) {
        return ResponseEntity.status(status)
                .body(new ErroResponse(status.value(), erro, List.of(detalhe)));
    }
}
