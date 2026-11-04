package com.adega.api;

import com.adega.model.Bebida;
import com.adega.repository.BebidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BebidaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BebidaRepository repositorio;

    @BeforeEach
    void limparBanco() {
        repositorio.deleteAll();
    }

    @Test
    void listaVaziaRetornaArrayVazio() throws Exception {
        mockMvc.perform(get("/api/bebidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void cadastraBebida() throws Exception {
        mockMvc.perform(post("/api/bebidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Vinho Tinto", "preco": 25.90, "quantEstoque": 10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Vinho Tinto"))
                .andExpect(jsonPath("$.preco").value(25.9))
                .andExpect(jsonPath("$.quantEstoque").value(10))
                .andExpect(jsonPath("$.valorEmEstoque").value(259.0));
    }

    @Test
    void naoCadastraNomeDuplicado() throws Exception {
        salvarBebida("Vinho Tinto", "25.90", 10);

        mockMvc.perform(post("/api/bebidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "vinho tinto", "preco": 30.00, "quantEstoque": 5}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void naoCadastraDadosInvalidos() throws Exception {
        mockMvc.perform(post("/api/bebidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": " ", "preco": 0, "quantEstoque": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detalhes").isArray());
    }

    @Test
    void buscaBebidaPorId() throws Exception {
        Bebida bebida = salvarBebida("Cerveja", "8.50", 24);

        mockMvc.perform(get("/api/bebidas/{id}", bebida.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cerveja"))
                .andExpect(jsonPath("$.quantEstoque").value(24));
    }

    @Test
    void buscaBebidaInexistente() throws Exception {
        mockMvc.perform(get("/api/bebidas/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void atualizaBebida() throws Exception {
        Bebida bebida = salvarBebida("Cerveja", "8.50", 24);

        mockMvc.perform(put("/api/bebidas/{id}", bebida.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Cerveja Pilsen", "preco": 9.90, "quantEstoque": 30}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cerveja Pilsen"))
                .andExpect(jsonPath("$.preco").value(9.9))
                .andExpect(jsonPath("$.quantEstoque").value(30));
    }

    @Test
    void naoAtualizaParaNomeDeOutraBebida() throws Exception {
        salvarBebida("Vinho", "25.90", 10);
        Bebida cerveja = salvarBebida("Cerveja", "8.50", 24);

        mockMvc.perform(put("/api/bebidas/{id}", cerveja.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Vinho", "preco": 9.90, "quantEstoque": 30}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void removeBebida() throws Exception {
        Bebida bebida = salvarBebida("Cerveja", "8.50", 24);

        mockMvc.perform(delete("/api/bebidas/{id}", bebida.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/bebidas/{id}", bebida.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void vendeBebida() throws Exception {
        Bebida bebida = salvarBebida("Vinho", "25.90", 10);

        mockMvc.perform(post("/api/bebidas/{id}/venda", bebida.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade": 3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeVendida").value(3))
                .andExpect(jsonPath("$.valorTotal").value(77.7))
                .andExpect(jsonPath("$.quantEstoque").value(7));
    }

    @Test
    void naoVendeSemEstoque() throws Exception {
        Bebida bebida = salvarBebida("Vinho", "25.90", 2);

        mockMvc.perform(post("/api/bebidas/{id}/venda", bebida.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade": 3}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void repoeEstoque() throws Exception {
        Bebida bebida = salvarBebida("Vinho", "25.90", 2);

        mockMvc.perform(post("/api/bebidas/{id}/reposicao", bebida.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade": 5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantEstoque").value(7));
    }

    @Test
    void vendeBebidaInexistente() throws Exception {
        mockMvc.perform(post("/api/bebidas/{id}/venda", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade": 1}
                                """))
                .andExpect(status().isNotFound());
    }

    private Bebida salvarBebida(String nome, String preco, int quantEstoque) {
        return repositorio.save(new Bebida(nome, new BigDecimal(preco), quantEstoque));
    }
}
