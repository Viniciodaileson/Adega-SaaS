package com.adega.security.jwt;

import com.adega.security.user.UsuarioRepository;
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
    private static final int TAMANHO_MAXIMO_TOKEN = 4096;

    private final JwtService servico;
    private final UsuarioRepository usuarios;

    public JwtAuthenticationFilter(JwtService servico, UsuarioRepository usuarios) {
        this.servico = servico;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {

        String cabecalho = requisicao.getHeader(HttpHeaders.AUTHORIZATION);

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {

            String token = cabecalho.substring(PREFIXO.length()).trim();

            if (!token.isEmpty() && token.length() <= TAMANHO_MAXIMO_TOKEN) {

                try {
                    JWTClaimsSet declaracoes = servico.validarAccessToken(token);

                    Long usuarioId = Long.valueOf(declaracoes.getSubject());

                    // Um token válido de um usuário removido ou inativado deixa de ser aceito.
                    if (usuarios.existsByIdAndAtivoTrue(usuarioId)) {

                        UsernamePasswordAuthenticationToken autenticacao =
                                new UsernamePasswordAuthenticationToken(
                                        declaracoes.getSubject(),
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_USUARIO"))
                                );

                        autenticacao.setDetails(declaracoes.getStringClaim("login"));
                        SecurityContextHolder.getContext().setAuthentication(autenticacao);
                    } else {
                        SecurityContextHolder.clearContext();
                    }

                } catch (RuntimeException | ParseException excecao) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        cadeia.doFilter(requisicao, resposta);
    }
}
