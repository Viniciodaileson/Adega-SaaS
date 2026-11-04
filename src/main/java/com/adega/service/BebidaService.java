package com.adega.service;

import com.adega.model.Bebida;
import com.adega.model.Resultado;
import com.adega.repository.BebidaRepository;

import java.math.BigDecimal;
import java.util.List;

public class BebidaService {

    private final BebidaRepository repositorio = new BebidaRepository();

    public Resultado cadastrar(String nome, BigDecimal preco, int quantEstoque) {

        if (repositorio.buscarPorNome(nome) != null) {
            return Resultado.falha("Já existe uma bebida cadastrada com esse nome.");
        }

        Bebida salva = repositorio.salvar(new Bebida(0, nome, preco, quantEstoque));

        return Resultado.ok("Bebida cadastrada com sucesso! ID: " + salva.getId());
    }

    public List<Bebida> listar() {
        return repositorio.listar();
    }

    public Bebida buscarPorNome(String nome) {
        return repositorio.buscarPorNome(nome);
    }

    public Resultado vender(String nome, int quantidade) {

        Bebida bebida = repositorio.buscarPorNome(nome);

        if (bebida == null) {
            return Resultado.falha("Bebida não encontrada: " + nome);
        }

        Resultado resultado = bebida.vender(quantidade);

        if (!resultado.sucesso()) {
            return resultado;
        }

        repositorio.atualizarEstoque(bebida);

        BigDecimal total = bebida.getPreco().multiply(BigDecimal.valueOf(quantidade));

        return Resultado.ok(resultado.mensagem(), total);
    }

    public Resultado repor(String nome, int quantidade) {

        Bebida bebida = repositorio.buscarPorNome(nome);

        if (bebida == null) {
            return Resultado.falha("Bebida não encontrada: " + nome);
        }

        Resultado resultado = bebida.reporEstoque(quantidade);

        if (resultado.sucesso()) {
            repositorio.atualizarEstoque(bebida);
        }

        return resultado;
    }
}
