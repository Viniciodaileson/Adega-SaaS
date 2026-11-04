package com.adega.security.jwt;

import com.nimbusds.jwt.JWTClaimsSet;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService servico;

    public JwtAuthenticationFilter(JwtService servico) {
        this.servico = servico;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {

        String cabecalho = requisicao.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {

            try {
                String token = cabecalho.substring(PREFIXO.length()).trim();
                JWTClaimsSet declaracoes = servico.validarAccessToken(token);

                UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                        declaracoes.getSubject(),
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USUARIO"))
                );

                autenticacao.setDetails(declaracoes.getStringClaim("login"));
                SecurityContextHolder.getContext().setAuthentication(autenticacao);

            } catch (RuntimeException | ParseException excecao) {
                SecurityContextHolder.clearContext();
            }
        }

        cadeia.doFilter(requisicao, resposta);
    }
}
