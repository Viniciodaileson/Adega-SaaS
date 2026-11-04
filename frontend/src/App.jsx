import { useEffect, useState } from "react"
import Login from "./componentes/Login"
import PainelBebidas from "./componentes/PainelBebidas"
import { encerrarSessao } from "./servicos/api"
import {
  inscreverOuvinte,
  limparTokens,
  obterRefreshToken,
  possuiSessao
} from "./servicos/tokens"

function App() {
  const [autenticado, definirAutenticado] = useState(possuiSessao())
  const [saindo, definirSaindo] = useState(false)

  useEffect(() => inscreverOuvinte(definirAutenticado), [])

  async function sair() {
    definirSaindo(true)

    try {
      await encerrarSessao(obterRefreshToken())
    } catch (falha) {
      definirSaindo(false)
    } finally {
      limparTokens()
      definirAutenticado(false)
      definirSaindo(false)
    }
  }

  if (!autenticado) {
    return <Login aoEntrar={() => definirAutenticado(true)} />
  }

  return <PainelBebidas aoSair={sair} saindo={saindo} />
}

export default App
