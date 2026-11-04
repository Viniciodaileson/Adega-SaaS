import { useState } from "react"
import { autenticar } from "../servicos/api"
import { extrairMensagemErro } from "../servicos/erros"
import { definirTokens } from "../servicos/tokens"

function Login({ aoEntrar }) {
  const [usuario, definirUsuario] = useState("")
  const [senha, definirSenha] = useState("")
  const [erro, definirErro] = useState("")
  const [carregando, definirCarregando] = useState(false)

  async function enviar(evento) {
    evento.preventDefault()
    definirErro("")

    if (!usuario.trim() || !senha) {
      definirErro("Informe o usuário e a senha.")
      return
    }

    definirCarregando(true)

    try {
      const resposta = await autenticar(usuario.trim(), senha)
      definirTokens(resposta)
      definirSenha("")
      aoEntrar()
    } catch (falha) {
      definirErro(extrairMensagemErro(falha))
    } finally {
      definirCarregando(false)
    }
  }

  return (
    <main className="tela-login">
      <section className="cartao-login">
        <header className="cabecalho-login">
          <span className="simbolo-adega" aria-hidden="true">
            🍷
          </span>
          <h1>AdegaSaaS</h1>
          <p>Gestão de bebidas com segurança de ponta a ponta.</p>
        </header>

        <form className="formulario" onSubmit={enviar} noValidate>
          <label className="campo" htmlFor="usuario">
            <span>Usuário</span>
            <input
              id="usuario"
              name="usuario"
              type="text"
              autoComplete="username"
              placeholder="Digite seu usuário"
              value={usuario}
              onChange={(evento) => definirUsuario(evento.target.value)}
              disabled={carregando}
              autoFocus
            />
          </label>

          <label className="campo" htmlFor="senha">
            <span>Senha</span>
            <input
              id="senha"
              name="senha"
              type="password"
              autoComplete="current-password"
              placeholder="Digite sua senha"
              value={senha}
              onChange={(evento) => definirSenha(evento.target.value)}
              disabled={carregando}
            />
          </label>

          {erro ? (
            <p className="mensagem-erro" role="alert">
              {erro}
            </p>
          ) : null}

          <button className="botao-principal" type="submit" disabled={carregando}>
            {carregando ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <footer className="rodape-login">
          <p>A sessão é mantida apenas na memória do navegador.</p>
          <p>Ao recarregar a página, será necessário entrar novamente.</p>
        </footer>
      </section>
    </main>
  )
}

export default Login
