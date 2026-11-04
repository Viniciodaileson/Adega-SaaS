package com.adega.service;

import com.adega.dto.BebidaRequest;
import com.adega.dto.BebidaResponse;
import com.adega.dto.VendaResponse;
import com.adega.exception.ConflitoException;
import com.adega.exception.RecursoNaoEncontradoException;
import com.adega.model.Bebida;
import com.adega.repository.BebidaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BebidaService {

    private final BebidaRepository repositorio;

    public BebidaService(BebidaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public BebidaResponse cadastrar(BebidaRequest request) {

        if (repositorio.existsByNomeIgnoreCase(request.nome())) {
            throw new ConflitoException("Já existe uma bebida cadastrada com esse nome.");
        }

        Bebida bebida = new Bebida(request.nome(), request.preco(), request.quantEstoque());

        return BebidaResponse.de(repositorio.save(bebida));
    }

    @Transactional(readOnly = true)
    public List<BebidaResponse> listar() {
        return repositorio.findAll(Sort.by("id"))
                .stream()
                .map(BebidaResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public BebidaResponse buscarPorId(Long id) {
        return BebidaResponse.de(buscarEntidade(id));
    }

    @Transactional
    public BebidaResponse atualizar(Long id, BebidaRequest request) {

        Bebida bebida = buscarEntidade(id);

        if (repositorio.existsByNomeIgnoreCaseAndIdNot(request.nome(), id)) {
            throw new ConflitoException("Já existe uma bebida cadastrada com esse nome.");
        }

        bebida.setNome(request.nome());
        bebida.setPreco(request.preco());
        bebida.setQuantEstoque(request.quantEstoque());

        return BebidaResponse.de(repositorio.save(bebida));
    }

    @Transactional
    public void remover(Long id) {
        repositorio.delete(buscarEntidade(id));
    }

    @Transactional
    public VendaResponse vender(Long id, int quantidade) {

        Bebida bebida = buscarEntidade(id);
        BigDecimal valorTotal = bebida.vender(quantidade);

        repositorio.save(bebida);

        return VendaResponse.de(bebida, quantidade, valorTotal);
    }

    @Transactional
    public BebidaResponse repor(Long id, int quantidade) {

        Bebida bebida = buscarEntidade(id);
        bebida.reporEstoque(quantidade);

        return BebidaResponse.de(repositorio.save(bebida));
    }

    private Bebida buscarEntidade(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bebida não encontrada: " + id));
    }
}
