package com.adega.security.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

public class AuditFilter extends OncePerRequestFilter {

    private final ApplicationEventPublisher publicador;

    public AuditFilter(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        return !requisicao.getRequestURI().startsWith("/api");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {

        try {
            cadeia.doFilter(requisicao, resposta);
        } finally {
            publicar(requisicao, resposta);
        }
    }

    private void publicar(HttpServletRequest requisicao, HttpServletResponse resposta) {

        try {
            Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

            String assunto = autenticacao != null
                    && autenticacao.isAuthenticated()
                    && !(autenticacao instanceof AnonymousAuthenticationToken)
                    ? autenticacao.getName()
                    : null;

            publicador.publishEvent(new AuditLogEvent(
                    assunto,
                    requisicao.getMethod(),
                    requisicao.getRequestURI(),
                    requisicao.getRemoteAddr(),
                    resposta.getStatus(),
                    Instant.now()
            ));

        } catch (RuntimeException ignorada) {
        }
    }
}
