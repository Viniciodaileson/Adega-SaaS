package com.adega.security.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LimiteTentativasFilter extends OncePerRequestFilter {

    private static final int MAXIMO_FALHAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final Duration BLOQUEIO = Duration.ofMinutes(15);

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();

    private volatile Instant proximaLimpeza = Instant.now();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest requisicao) {
        return !"/api/auth/login".equals(requisicao.getRequestURI())
                || !"POST".equalsIgnoreCase(requisicao.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requisicao,
                                    HttpServletResponse resposta,
                                    FilterChain cadeia) throws ServletException, IOException {

        String ip = requisicao.getRemoteAddr();

        if (estaBloqueado(ip)) {
            escreverErro(resposta);
            return;
        }

        cadeia.doFilter(requisicao, resposta);

        if (resposta.getStatus() == HttpStatus.UNAUTHORIZED.value()) {
            registrarFalha(ip);
        } else if (resposta.getStatus() == HttpStatus.OK.value()) {
            registros.remove(ip);
        }
    }

    public void limpar() {
        registros.clear();
        proximaLimpeza = Instant.now();
    }

    private boolean estaBloqueado(String ip) {

        limparExpirados();

        Registro registro = registros.get(ip);

        if (registro == null || registro.bloqueadoAte() == null) {
            return false;
        }

        if (registro.bloqueadoAte().isAfter(Instant.now())) {
            return true;
        }

        registros.remove(ip);
        return false;
    }

    private void registrarFalha(String ip) {

        registros.compute(ip, (chave, atual) -> {

            Instant agora = Instant.now();

            if (atual == null || atual.inicio().plus(JANELA).isBefore(agora)) {
                return new Registro(1, agora, null);
            }

            int falhas = atual.falhas() + 1;
            Instant bloqueadoAte = falhas >= MAXIMO_FALHAS ? agora.plus(BLOQUEIO) : null;

            return new Registro(falhas, atual.inicio(), bloqueadoAte);
        });
    }

    private void limparExpirados() {

        Instant agora = Instant.now();

        if (agora.isBefore(proximaLimpeza)) {
            return;
        }

        proximaLimpeza = agora.plus(Duration.ofMinutes(1));
        registros.entrySet().removeIf(entrada -> expirado(entrada.getValue(), agora));
    }

    private boolean expirado(Registro registro, Instant agora) {

        if (registro.bloqueadoAte() != null) {
            return registro.bloqueadoAte().isBefore(agora);
        }

        return registro.inicio().plus(JANELA).isBefore(agora);
    }

    private void escreverErro(HttpServletResponse resposta) throws IOException {

        resposta.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());

        resposta.getWriter().write("{\"status\":429,\"erro\":\"Muitas tentativas\""
                + ",\"detalhes\":[\"Aguarde alguns minutos antes de tentar novamente.\"]}");
    }

    private record Registro(int falhas, Instant inicio, Instant bloqueadoAte) {
    }
}
