package com.adega.security.auth;

import com.adega.security.exception.AcessoNegadoException;
import com.adega.security.exception.CredenciaisInvalidasException;
import com.adega.security.exception.TokenInvalidoException;
import com.adega.security.jwt.JwtProperties;
import com.adega.security.jwt.JwtService;
import com.adega.security.refresh.RefreshTokenService;
import com.adega.security.refresh.RotacaoRefreshToken;
import com.adega.security.user.Usuario;
import com.adega.security.user.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder codificador;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final JwtProperties propriedades;
    private final String hashFicticio;

    public AuthService(UsuarioRepository usuarios,
                       PasswordEncoder codificador,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       JwtProperties propriedades) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.propriedades = propriedades;
        this.hashFicticio = codificador.encode(UUID.randomUUID().toString());
    }

    public LoginResponse autenticar(LoginRequest requisicao) {

        String loginNormalizado = requisicao.login().trim().toLowerCase(Locale.ROOT);
        Optional<Usuario> encontrado = usuarios.findByLogin(loginNormalizado);

        String hash = encontrado.map(Usuario::getHashSenha).orElse(hashFicticio);
        boolean senhaConfere = codificador.matches(requisicao.senha(), hash);

        if (encontrado.isEmpty() || !senhaConfere) {
            throw new CredenciaisInvalidasException("Login ou senha inválidos.");
        }

        Usuario usuario = encontrado.get();

        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException("Usuário inativo.");
        }

        String refreshToken = refreshTokenService.criarRefreshToken(usuario.getId());

        return emitir(usuario, refreshToken);
    }

    public LoginResponse atualizarToken(RefreshRequest requisicao) {

        RotacaoRefreshToken rotacao = refreshTokenService.rotacionarToken(requisicao.refreshToken());

        Usuario usuario = usuarios.findById(rotacao.usuarioId())
                .orElseThrow(() -> new TokenInvalidoException("Usuário do token não encontrado."));

        if (!usuario.isAtivo()) {
            refreshTokenService.revogarTodosDoUsuario(usuario.getId());
            throw new AcessoNegadoException("Usuário inativo.");
        }

        return emitir(usuario, rotacao.novoToken());
    }

    private LoginResponse emitir(Usuario usuario, String refreshToken) {

        String accessToken = jwtService.emitirAccessToken(usuario.getId(), usuario.getLogin());
        long expiresIn = propriedades.getTempoVidaMinutos() * 60;

        return new LoginResponse(accessToken, refreshToken, expiresIn);
    }
}
