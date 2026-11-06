package com.adega.security.auth;

import com.adega.security.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthService servico;
    private final JwtService jwtService;

    public AuthController(AuthService servico, JwtService jwtService) {
        this.servico = servico;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest requisicao,
                               HttpServletResponse resposta) {
        impedirCache(resposta);
        return servico.autenticar(requisicao);
    }

    @PostMapping("/api/auth/refresh")
    public LoginResponse atualizarToken(@Valid @RequestBody RefreshRequest requisicao,
                                        HttpServletResponse resposta) {
        impedirCache(resposta);
        return servico.atualizarToken(requisicao);
    }

    @PostMapping("/api/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sair(@Valid @RequestBody RefreshRequest requisicao,
                     HttpServletResponse resposta) {
        impedirCache(resposta);
        servico.encerrarSessao(requisicao);
    }

    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public String jwks() {
        return jwtService.obterJwksPublico();
    }

    private void impedirCache(HttpServletResponse resposta) {
        resposta.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        resposta.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }
}
