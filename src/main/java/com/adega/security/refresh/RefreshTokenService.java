package com.adega.security.refresh;

import com.adega.security.exception.ReutilizacaoTokenException;
import com.adega.security.exception.TokenInvalidoException;
import com.adega.security.jwt.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class RefreshTokenService {

    private static final Logger REGISTRO = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final SecureRandom GERADOR = new SecureRandom();
    private static final Base64.Encoder CODIFICADOR = Base64.getUrlEncoder().withoutPadding();

    private final RefreshTokenRepository repositorio;
    private final JwtProperties propriedades;

    public RefreshTokenService(RefreshTokenRepository repositorio, JwtProperties propriedades) {
        this.repositorio = repositorio;
        this.propriedades = propriedades;
    }

    @Transactional
    public String criarRefreshToken(Long usuarioId) {

        String token = gerarToken();

        RefreshToken registro = new RefreshToken(
                usuarioId,
                gerarHash(token),
                Instant.now().plus(Duration.ofDays(propriedades.getTempoVidaRefreshDias()))
        );

        repositorio.save(registro);

        return token;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = TokenInvalidoException.class)
    public RotacaoRefreshToken rotacionarToken(String tokenRecebido) {

        RefreshToken atual = repositorio.buscarPorHashTokenComBloqueio(gerarHash(tokenRecebido))
                .orElseThrow(() -> new TokenInvalidoException("Refresh token inválido."));

        if (atual.isRevogado()) {
            repositorio.revogarTodosDoUsuario(atual.getUsuarioId());
            throw new ReutilizacaoTokenException(
                    "Refresh token reutilizado. Todos os tokens do usuário foram revogados."
            );
        }

        if (atual.getExpiraEm().isBefore(Instant.now())) {
            atual.setRevogado(true);
            repositorio.save(atual);
            throw new TokenInvalidoException("Refresh token expirado.");
        }

        String novoToken = gerarToken();
        String novoHash = gerarHash(novoToken);

        RefreshToken novoRegistro = new RefreshToken(
                atual.getUsuarioId(),
                novoHash,
                Instant.now().plus(Duration.ofDays(propriedades.getTempoVidaRefreshDias()))
        );

        repositorio.save(novoRegistro);

        atual.setRevogado(true);
        atual.setSubstituidoPor(novoHash);
        repositorio.save(atual);

        return new RotacaoRefreshToken(atual.getUsuarioId(), novoToken);
    }

    @Transactional
    public void revogarTodosDoUsuario(Long usuarioId) {
        repositorio.revogarTodosDoUsuario(usuarioId);
    }

    @Transactional
    public void revogarPorToken(String tokenRecebido) {

        repositorio.findByHashToken(gerarHash(tokenRecebido))
                .ifPresent(registro -> repositorio.revogarTodosDoUsuario(registro.getUsuarioId()));
    }

    /**
     * Remove tokens expirados e tokens revogados há mais de 30 dias,
     * evitando crescimento indefinido da tabela.
     */
    @Scheduled(cron = "${adega.refresh.limpeza-cron:0 0 3 * * *}")
    @Transactional
    public void limparTokensExpirados() {

        Instant agora = Instant.now();
        int removidos = repositorio.limparTokensAntigos(agora, agora.minus(Duration.ofDays(30)));

        if (removidos > 0) {
            REGISTRO.info("Rotina de limpeza removeu {} refresh tokens antigos.", removidos);
        }
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        GERADOR.nextBytes(bytes);
        return CODIFICADOR.encodeToString(bytes);
    }

    private String gerarHash(String token) {

        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(resumo);

        } catch (NoSuchAlgorithmException excecao) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível.", excecao);
        }
    }
}
