package com.adega.security.jwt;

import com.adega.security.exception.TokenInvalidoException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Duration TOLERANCIA_RELOGIO = Duration.ofSeconds(30);

    private final JwtProperties propriedades;
    private final JwtKeyProvider provedorChaves;

    public JwtService(JwtProperties propriedades, JwtKeyProvider provedorChaves) {
        this.propriedades = propriedades;
        this.provedorChaves = provedorChaves;
    }

    public String emitirAccessToken(Long idUsuario, String login) {

        try {
            Instant agora = Instant.now();

            JWTClaimsSet declaracoes = new JWTClaimsSet.Builder()
                    .subject(String.valueOf(idUsuario))
                    .claim("login", login)
                    .issuer(propriedades.getEmissor())
                    .issueTime(Date.from(agora))
                    .expirationTime(Date.from(agora.plus(Duration.ofMinutes(propriedades.getTempoVidaMinutos()))))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            SignedJWT token = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .keyID(provedorChaves.getChave().getKeyID())
                            .type(JOSEObjectType.JWT)
                            .build(),
                    declaracoes
            );

            token.sign(new ECDSASigner(provedorChaves.getChave()));

            return token.serialize();

        } catch (JOSEException excecao) {
            throw new IllegalStateException("Não foi possível emitir o token de acesso.", excecao);
        }
    }

    public JWTClaimsSet validarAccessToken(String token) {

        try {
            SignedJWT assinado = SignedJWT.parse(token);

            if (!JWSAlgorithm.ES256.equals(assinado.getHeader().getAlgorithm())) {
                throw new TokenInvalidoException("Algoritmo de assinatura inválido.");
            }

            if (!assinado.verify(new ECDSAVerifier(provedorChaves.getChavePublica()))) {
                throw new TokenInvalidoException("Assinatura do token inválida.");
            }

            JWTClaimsSet declaracoes = assinado.getJWTClaimsSet();

            if (!propriedades.getEmissor().equals(declaracoes.getIssuer())) {
                throw new TokenInvalidoException("Emissor do token inválido.");
            }

            Date expiracao = declaracoes.getExpirationTime();

            if (expiracao == null
                    || expiracao.toInstant().isBefore(Instant.now().minus(TOLERANCIA_RELOGIO))) {
                throw new TokenInvalidoException("Token expirado.");
            }

            if (declaracoes.getSubject() == null || declaracoes.getSubject().isBlank()) {
                throw new TokenInvalidoException("Token sem identificação de usuário.");
            }

            return declaracoes;

        } catch (ParseException | JOSEException excecao) {
            throw new TokenInvalidoException("Token inválido.");
        }
    }

    public String obterJwksPublico() {
        return new JWKSet(provedorChaves.getChavePublica()).toString();
    }
}
