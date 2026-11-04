package com.adega.security.auth;

import com.adega.security.refresh.RefreshToken;
import com.adega.security.refresh.RefreshTokenRepository;
import com.adega.security.user.Usuario;
import com.adega.security.user.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AutenticacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarios;

    @Autowired
    private RefreshTokenRepository refreshTokens;

    @Autowired
    private PasswordEncoder codificador;

    @BeforeEach
    void preparar() {
        refreshTokens.deleteAll();
        usuarios.deleteAll();
        usuarios.save(new Usuario("teste", codificador.encode("Senha@123")));
    }

    @Test
    void loginComCredenciaisValidasRetornaTokens() throws Exception {

        JsonNode resposta = corpo(efetuarLogin("teste", "Senha@123"));

        assertFalse(resposta.get("accessToken").asText().isBlank());
        assertFalse(resposta.get("refreshToken").asText().isBlank());
        assertEquals(900, resposta.get("expiresIn").asLong());
    }

    @Test
    void accessTokenUsaEs256() throws Exception {

        String token = corpo(efetuarLogin("teste", "Senha@123")).get("accessToken").asText();
        String cabecalho = new String(
                Base64.getUrlDecoder().decode(token.split("\\.")[0]),
                StandardCharsets.UTF_8
        );

        assertTrue(cabecalho.contains("\"alg\":\"ES256\""));
        assertTrue(cabecalho.contains("\"kid\""));
    }

    @Test
    void loginComSenhaInvalidaRetorna401() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("teste", "SenhaErrada")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginComUsuarioInexistenteRetorna401() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("fantasma", "Senha@123")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioInativoRetorna403() throws Exception {

        Usuario inativo = new Usuario("inativo", codificador.encode("Senha@123"));
        inativo.setAtivo(false);
        usuarios.save(inativo);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin("inativo", "Senha@123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void acessoSemTokenRetorna401() throws Exception {

        mockMvc.perform(get("/api/bebidas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Não autenticado"));
    }

    @Test
    void acessoComTokenRetorna200() throws Exception {

        String token = corpo(efetuarLogin("teste", "Senha@123")).get("accessToken").asText();

        mockMvc.perform(get("/api/bebidas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void tokenAdulteradoRetorna401() throws Exception {

        mockMvc.perform(get("/api/bebidas")
                        .header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void jwksExpoeSomenteChavePublica() throws Exception {

        mockMvc.perform(get("/.well-known/jwks.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("EC"))
                .andExpect(jsonPath("$.keys[0].crv").value("P-256"))
                .andExpect(jsonPath("$.keys[0].d").doesNotExist());
    }

    @Test
    void refreshRotacionaToken() throws Exception {

        String refreshInicial = corpo(efetuarLogin("teste", "Senha@123")).get("refreshToken").asText();

        MvcResult resultado = efetuarRefresh(refreshInicial);

        assertEquals(200, resultado.getResponse().getStatus());

        JsonNode resposta = corpo(resultado);
        String refreshNovo = resposta.get("refreshToken").asText();

        assertNotEquals(refreshInicial, refreshNovo);
        assertFalse(resposta.get("accessToken").asText().isBlank());
    }

    @Test
    void reusoDeRefreshRevogaTodosOsTokens() throws Exception {

        String refreshInicial = corpo(efetuarLogin("teste", "Senha@123")).get("refreshToken").asText();

        MvcResult rotacao = efetuarRefresh(refreshInicial);
        assertEquals(200, rotacao.getResponse().getStatus());
        String refreshNovo = corpo(rotacao).get("refreshToken").asText();

        MvcResult reuso = efetuarRefresh(refreshInicial);
        assertEquals(401, reuso.getResponse().getStatus());

        MvcResult aposReuso = efetuarRefresh(refreshNovo);
        assertEquals(401, aposReuso.getResponse().getStatus());

        Usuario usuario = usuarios.findByLogin("teste").orElseThrow();
        List<RefreshToken> tokens = refreshTokens.findByUsuarioId(usuario.getId());

        assertFalse(tokens.isEmpty());
        assertTrue(tokens.stream().allMatch(RefreshToken::isRevogado));
    }

    @Test
    void refreshInvalidoRetorna401() throws Exception {

        MvcResult resultado = efetuarRefresh("token-que-nao-existe");

        assertEquals(401, resultado.getResponse().getStatus());
    }

    private MvcResult efetuarLogin(String login, String senha) throws Exception {

        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoLogin(login, senha)))
                .andReturn();
    }

    private MvcResult efetuarRefresh(String token) throws Exception {

        return mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest(token))))
                .andReturn();
    }

    private String corpoLogin(String login, String senha) throws Exception {
        return objectMapper.writeValueAsString(new LoginRequest(login, senha));
    }

    private JsonNode corpo(MvcResult resultado) throws Exception {
        return objectMapper.readTree(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }
}
