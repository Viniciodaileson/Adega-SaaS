package com.adega.security.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class CargaInicialAdmin implements CommandLineRunner {

    private static final Logger REGISTRO = LoggerFactory.getLogger(CargaInicialAdmin.class);
    private static final String LOGIN_ADMIN = "admin";
    private static final SecureRandom GERADOR = new SecureRandom();

    private final UsuarioRepository repositorio;
    private final PasswordEncoder codificador;
    private final String senhaConfigurada;

    public CargaInicialAdmin(UsuarioRepository repositorio,
                             PasswordEncoder codificador,
                             @Value("${adega.admin.senha:}") String senhaConfigurada) {
        this.repositorio = repositorio;
        this.codificador = codificador;
        this.senhaConfigurada = senhaConfigurada;
    }

    @Override
    public void run(String... argumentos) {

        if (repositorio.existsByLogin(LOGIN_ADMIN)) {
            return;
        }

        boolean senhaGerada = senhaConfigurada == null || senhaConfigurada.isBlank();
        String senha = senhaGerada ? gerarSenhaForte() : senhaConfigurada;

        repositorio.save(new Usuario(LOGIN_ADMIN, codificador.encode(senha)));

        REGISTRO.info("Usuário administrador criado com login \"{}\".", LOGIN_ADMIN);

        if (senhaGerada) {
            System.out.println("============================================================");
            System.out.println(" USUARIO ADMINISTRADOR CRIADO");
            System.out.println(" Login: " + LOGIN_ADMIN);
            System.out.println(" Senha: " + senha);
            System.out.println(" Guarde esta senha. Ela nao sera exibida novamente.");
            System.out.println("============================================================");
        }
    }

    private String gerarSenhaForte() {
        byte[] bytes = new byte[24];
        GERADOR.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
