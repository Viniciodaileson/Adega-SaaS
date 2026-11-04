package com.adega.security.jwt;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.springframework.stereotype.Component;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.UUID;

@Component
public class JwtKeyProvider {

    private final ECKey chave;

    public JwtKeyProvider() {

        try {
            KeyPairGenerator gerador = KeyPairGenerator.getInstance("EC");
            gerador.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            KeyPair par = gerador.generateKeyPair();

            this.chave = new ECKey.Builder(Curve.P_256, (ECPublicKey) par.getPublic())
                    .privateKey((ECPrivateKey) par.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();

        } catch (GeneralSecurityException excecao) {
            throw new IllegalStateException("Não foi possível gerar o par de chaves ES256.", excecao);
        }
    }

    public ECKey getChave() {
        return chave;
    }

    public ECKey getChavePublica() {
        return chave.toPublicJWK();
    }
}
