package com.adega.api;

import com.adega.dto.BebidaRequest;
import com.adega.dto.BebidaResponse;
import com.adega.dto.ReposicaoRequest;
import com.adega.dto.VendaRequest;
import com.adega.dto.VendaResponse;
import com.adega.service.BebidaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bebidas")
public class BebidaController {

    private final BebidaService servico;

    public BebidaController(BebidaService servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<BebidaResponse> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    public BebidaResponse buscar(@PathVariable Long id) {
        return servico.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BebidaResponse cadastrar(@Valid @RequestBody BebidaRequest request) {
        return servico.cadastrar(request);
    }

    @PutMapping("/{id}")
    public BebidaResponse atualizar(@PathVariable Long id, @Valid @RequestBody BebidaRequest request) {
        return servico.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable Long id) {
        servico.remover(id);
    }

    @PostMapping("/{id}/venda")
    public VendaResponse vender(@PathVariable Long id, @Valid @RequestBody VendaRequest request) {
        return servico.vender(id, request.quantidade());
    }

    @PostMapping("/{id}/reposicao")
    public BebidaResponse repor(@PathVariable Long id, @Valid @RequestBody ReposicaoRequest request) {
        return servico.repor(id, request.quantidade());
    }
}
