import { useCallback, useEffect, useState } from "react"
import {
  cadastrarBebida,
  excluirBebida,
  listarBebidas,
  reporEstoque,
  venderUnidade
} from "../servicos/api"
import { extrairMensagemErro } from "../servicos/erros"
import { formatarMoeda, formatarQuantidade } from "../servicos/formatacao"

function PainelBebidas({ aoSair, saindo }) {
  const [bebidas, definirBebidas] = useState([])
  const [carregando, definirCarregando] = useState(true)
  const [salvando, definirSalvando] = useState(false)
  const [acaoEmAndamento, definirAcaoEmAndamento] = useState(null)
  const [erro, definirErro] = useState("")
  const [aviso, definirAviso] = useState("")
  const [nome, definirNome] = useState("")
  const [preco, definirPreco] = useState("")
  const [quantidade, definirQuantidade] = useState("")

  const carregar = useCallback(async () => {
    definirCarregando(true)

    try {
      const dados = await listarBebidas()
      definirBebidas(Array.isArray(dados) ? dados : [])
    } catch (falha) {
      definirErro(extrairMensagemErro(falha))
    } finally {
      definirCarregando(false)
    }
  }, [])

  useEffect(() => {
    carregar()
  }, [carregar])

  function limparMensagens() {
    definirErro("")
    definirAviso("")
  }

  async function cadastrar(evento) {
    evento.preventDefault()
    limparMensagens()

    const precoConvertido = Number(String(preco).replace(",", "."))
    const quantidadeConvertida = Number(quantidade)

    if (!nome.trim()) {
      definirErro("Informe o nome da bebida.")
      return
    }

    if (!Number.isFinite(precoConvertido) || precoConvertido <= 0) {
      definirErro("Informe um preço maior que zero.")
      return
    }

    if (!Number.isInteger(quantidadeConvertida) || quantidadeConvertida < 0) {
      definirErro("Informe uma quantidade inteira maior ou igual a zero.")
      return
    }

    definirSalvando(true)

    try {
      await cadastrarBebida({
        nome: nome.trim(),
        preco: precoConvertido,
        quantEstoque: quantidadeConvertida
      })

      definirNome("")
      definirPreco("")
      definirQuantidade("")
      definirAviso("Bebida cadastrada com sucesso.")

      await carregar()
    } catch (falha) {
      definirErro(extrairMensagemErro(falha))
    } finally {
      definirSalvando(false)
    }
  }

  async function vender(bebida) {
    limparMensagens()
    definirAcaoEmAndamento(`venda-${bebida.id}`)

    try {
      const resultado = await venderUnidade(bebida.id)

      definirAviso(
        `Venda registrada: ${resultado.quantidadeVendida} unidade de ${resultado.nome} por ${formatarMoeda(resultado.valorTotal)}.`
      )

      await carregar()
    } catch (falha) {
      definirErro(extrairMensagemErro(falha))
    } finally {
      definirAcaoEmAndamento(null)
    }
  }

  async function repor(bebida) {
    limparMensagens()

    const entrada = window.prompt(
      `Quantas unidades de ${bebida.nome} deseja repor no estoque?`,
      "1"
    )

    if (entrada === null) {
      return
    }

    const quantidadeReposta = Number(entrada)

    if (!Number.isInteger(quantidadeReposta) || quantidadeReposta <= 0) {
      definirErro("Informe uma quantidade inteira maior que zero para a reposição.")
      return
    }

    definirAcaoEmAndamento(`reposicao-${bebida.id}`)

    try {
      await reporEstoque(bebida.id, quantidadeReposta)
      definirAviso(
        `Estoque de ${bebida.nome} reposto com ${formatarQuantidade(quantidadeReposta)} unidade(s).`
      )

      await carregar()
    } catch (falha) {
      definirErro(extrairMensagemErro(falha))
    } finally {
      definirAcaoEmAndamento(null)
    }
  }

  async function excluir(bebida) {
    if (!window.confirm(`Deseja excluir a bebida "${bebida.nome}"?`)) {
      return
    }

    limparMensagens()
    definirAcaoEmAndamento(`exclusao-${bebida.id}`)

    try {
      await excluirBebida(bebida.id)
      definirBebidas((atuais) => atuais.filter((item) => item.id !== bebida.id))
      definirAviso(`Bebida ${bebida.nome} excluída com sucesso.`)
    } catch (falha) {
      definirErro(extrairMensagemErro(falha))
    } finally {
      definirAcaoEmAndamento(null)
    }
  }

  const totalUnidades = bebidas.reduce(
    (total, bebida) => total + Number(bebida.quantEstoque ?? 0),
    0
  )

  const valorTotalEstoque = bebidas.reduce(
    (total, bebida) => total + Number(bebida.valorEmEstoque ?? 0),
    0
  )

  return (
    <div className="tela-painel">
      <header className="cabecalho-painel">
        <div className="marca-painel">
          <span className="simbolo-adega" aria-hidden="true">
            🍷
          </span>
          <div>
            <h1>AdegaSaaS</h1>
            <p>Painel de gerenciamento de bebidas</p>
          </div>
        </div>

        <button className="botao-secundario" type="button" onClick={aoSair} disabled={saindo}>
          {saindo ? "Saindo..." : "Sair"}
        </button>
      </header>

      <main className="conteudo-painel">
        <section className="resumo">
          <article className="cartao-resumo">
            <span>Bebidas cadastradas</span>
            <strong>{formatarQuantidade(bebidas.length)}</strong>
          </article>
          <article className="cartao-resumo">
            <span>Unidades em estoque</span>
            <strong>{formatarQuantidade(totalUnidades)}</strong>
          </article>
          <article className="cartao-resumo">
            <span>Valor total em estoque</span>
            <strong>{formatarMoeda(valorTotalEstoque)}</strong>
          </article>
        </section>

        <section className="bloco">
          <h2>Cadastrar nova bebida</h2>

          <form className="formulario-cadastro" onSubmit={cadastrar} noValidate>
            <label className="campo" htmlFor="nome">
              <span>Nome</span>
              <input
                id="nome"
                name="nome"
                type="text"
                placeholder="Ex.: Vinho Tinto Seco"
                maxLength={100}
                value={nome}
                onChange={(evento) => definirNome(evento.target.value)}
                disabled={salvando}
              />
            </label>

            <label className="campo" htmlFor="preco">
              <span>Preço (R$)</span>
              <input
                id="preco"
                name="preco"
                type="text"
                inputMode="decimal"
                placeholder="Ex.: 25,90"
                value={preco}
                onChange={(evento) => definirPreco(evento.target.value)}
                disabled={salvando}
              />
            </label>

            <label className="campo" htmlFor="quantidade">
              <span>Quantidade em estoque</span>
              <input
                id="quantidade"
                name="quantidade"
                type="number"
                min="0"
                step="1"
                placeholder="Ex.: 10"
                value={quantidade}
                onChange={(evento) => definirQuantidade(evento.target.value)}
                disabled={salvando}
              />
            </label>

            <button className="botao-principal" type="submit" disabled={salvando}>
              {salvando ? "Cadastrando..." : "Cadastrar"}
            </button>
          </form>
        </section>

        {erro ? (
          <p className="mensagem-erro" role="alert">
            {erro}
          </p>
        ) : null}

        {aviso ? (
          <p className="mensagem-sucesso" role="status">
            {aviso}
          </p>
        ) : null}

        <section className="bloco">
          <div className="cabecalho-tabela">
            <h2>Bebidas</h2>
            <button
              className="botao-secundario"
              type="button"
              onClick={carregar}
              disabled={carregando}
            >
              {carregando ? "Atualizando..." : "Atualizar lista"}
            </button>
          </div>

          <div className="tabela-rolagem">
            <table className="tabela-bebidas">
              <thead>
                <tr>
                  <th scope="col">ID</th>
                  <th scope="col">Nome</th>
                  <th scope="col">Preço</th>
                  <th scope="col">Quantidade em Estoque</th>
                  <th scope="col">Valor Total em Estoque</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {carregando && bebidas.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="tabela-vazia">
                      Carregando bebidas...
                    </td>
                  </tr>
                ) : null}

                {!carregando && bebidas.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="tabela-vazia">
                      Nenhuma bebida cadastrada ainda.
                    </td>
                  </tr>
                ) : null}

                {bebidas.map((bebida) => {
                  const vendendo = acaoEmAndamento === `venda-${bebida.id}`
                  const repondo = acaoEmAndamento === `reposicao-${bebida.id}`
                  const excluindo = acaoEmAndamento === `exclusao-${bebida.id}`
                  const ocupado = Boolean(acaoEmAndamento)

                  return (
                    <tr key={bebida.id}>
                      <td>{bebida.id}</td>
                      <td>{bebida.nome}</td>
                      <td>{formatarMoeda(bebida.preco)}</td>
                      <td>{formatarQuantidade(bebida.quantEstoque)}</td>
                      <td>{formatarMoeda(bebida.valorEmEstoque)}</td>
                      <td>
                        <div className="acoes">
                          <button
                            className="botao-acao"
                            type="button"
                            onClick={() => vender(bebida)}
                            disabled={ocupado || Number(bebida.quantEstoque) <= 0}
                          >
                            {vendendo ? "Vendendo..." : "Vender 1 Unidade"}
                          </button>
                          <button
                            className="botao-acao botao-acao-secundario"
                            type="button"
                            onClick={() => repor(bebida)}
                            disabled={ocupado}
                          >
                            {repondo ? "Repondo..." : "Repor Estoque"}
                          </button>
                          <button
                            className="botao-acao"
                            type="button"
                            onClick={() => excluir(bebida)}
                            disabled={ocupado}
                          >
                            {excluindo ? "Excluindo..." : "Excluir"}
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        </section>
      </main>
    </div>
  )
}

export default PainelBebidas
