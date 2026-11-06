package com.adega.security.jwt;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.text.ParseException;
import java.util.UUID;

@Component
public class JwtKeyProvider {

    private static final Logger REGISTRO = LoggerFactory.getLogger(JwtKeyProvider.class);

    private final ECKey chave;

    public JwtKeyProvider(JwtProperties propriedades) {
        this.chave = carregarOuGerar(propriedades);
    }

    private ECKey carregarOuGerar(JwtProperties propriedades) {

        String jwkConfigurado = propriedades.getChaveJwk();

        if (jwkConfigurado != null && !jwkConfigurado.isBlank()) {
            REGISTRO.info("Usando a chave ES256 definida em adega.jwt.chave-jwk.");
            return validar(lerJwk(jwkConfigurado));
        }

        String arquivoConfigurado = propriedades.getArquivoChave();

        if (arquivoConfigurado == null || arquivoConfigurado.isBlank()) {
            REGISTRO.warn("Chave ES256 efêmera em memória. Os tokens serão invalidados a cada reinício. "
                    + "Configure adega.jwt.arquivo-chave ou adega.jwt.chave-jwk.");
            return gerarChave();
        }

        Path arquivo = Path.of(arquivoConfigurado).toAbsolutePath().normalize();

        if (Files.exists(arquivo)) {
            try {
                return validar(lerJwk(Files.readString(arquivo)));
            } catch (IOException excecao) {
                throw new IllegalStateException(
                        "Não foi possível ler a chave ES256 em " + arquivo + ".", excecao);
            }
        }

        ECKey novaChave = gerarChave();

        try {
            Files.writeString(arquivo, novaChave.toJSONString(),
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
            restringirPermissoes(arquivo);
            REGISTRO.info("Chave ES256 criada em {}. Proteja este arquivo.", arquivo);
            return novaChave;
        } catch (FileAlreadyExistsException excecao) {
            // Outra instância criou a chave entre a verificação e a gravação.
            try {
                return validar(lerJwk(Files.readString(arquivo)));
            } catch (IOException falhaLeitura) {
                throw new IllegalStateException(
                        "Não foi possível ler a chave ES256 em " + arquivo + ".", falhaLeitura);
            }
        } catch (IOException excecao) {
            throw new IllegalStateException(
                    "Não foi possível gravar a chave ES256 em " + arquivo + ".", excecao);
        }
    }

    private ECKey lerJwk(String conteudo) {
        try {
            return ECKey.parse(conteudo);
        } catch (ParseException excecao) {
            throw new IllegalStateException("A chave ES256 configurada não é um JWK EC válido.", excecao);
        }
    }

    private ECKey validar(ECKey chaveLida) {

        if (!Curve.P_256.equals(chaveLida.getCurve())) {
            throw new IllegalStateException("A chave ES256 deve usar a curva P-256 (secp256r1).");
        }

        if (!chaveLida.isPrivate()) {
            throw new IllegalStateException("A chave ES256 configurada não contém a parte privada.");
        }

        if (chaveLida.getKeyID() == null || chaveLida.getKeyID().isBlank()) {
            return new ECKey.Builder(Curve.P_256, chaveLida.toECPublicKey())
                    .privateKey(chaveLida.toECPrivateKey())
                    .keyID(UUID.randomUUID().toString())
                    .build();
        }

        return chaveLida;
    }

    private ECKey gerarChave() {

        try {
            KeyPairGenerator gerador = KeyPairGenerator.getInstance("EC");
            gerador.initialize(new ECGenParameterSpec("secp256r1"), new SecureRandom());

            KeyPair par = gerador.generateKeyPair();

            return new ECKey.Builder(Curve.P_256, (ECPublicKey) par.getPublic())
                    .privateKey((ECPrivateKey) par.getPrivate())
                    .keyID(UUID.randomUUID().toString())
                    .build();

        } catch (GeneralSecurityException excecao) {
            throw new IllegalStateException("Não foi possível gerar o par de chaves ES256.", excecao);
        }
    }

    private void restringirPermissoes(Path arquivo) {
        try {
            Files.setPosixFilePermissions(arquivo, PosixFilePermissions.fromString("rw-------"));
        } catch (UnsupportedOperationException | IOException ignorada) {
            // Windows e alguns sistemas de arquivos não suportam permissões POSIX.
        }
    }

    public ECKey getChave() {
        return chave;
    }

    public ECKey getChavePublica() {
        return chave.toPublicJWK();
    }
}
