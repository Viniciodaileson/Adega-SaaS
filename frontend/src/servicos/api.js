import axios from "axios"
import {
  definirTokens,
  limparTokens,
  obterAccessToken,
  obterRefreshToken
} from "./tokens"

const ENDERECO_API = (
  import.meta.env.VITE_API_URL || "http://localhost:8080"
).replace(/\/+$/, "")

const api = axios.create({
  baseURL: ENDERECO_API,
  timeout: 15000
})

let renovacaoEmAndamento = null

api.interceptors.request.use((configuracao) => {
  const token = obterAccessToken()

  if (token) {
    configuracao.headers.Authorization = `Bearer ${token}`
  }

  return configuracao
})

api.interceptors.response.use(
  (resposta) => resposta,
  async (erro) => {
    const configuracao = erro?.config
    const status = erro?.response?.status
    const rota = configuracao?.url ?? ""
    const tokenRenovacao = obterRefreshToken()

    const podeRenovar =
      status === 401 &&
      Boolean(tokenRenovacao) &&
      Boolean(configuracao) &&
      !configuracao.tentativaRenovada &&
      !rota.startsWith("/api/auth/")

    if (!podeRenovar) {
      return Promise.reject(erro)
    }

    configuracao.tentativaRenovada = true

    try {
      if (!renovacaoEmAndamento) {
        renovacaoEmAndamento = axios
          .post(
            `${ENDERECO_API}/api/auth/refresh`,
            { refreshToken: tokenRenovacao },
            { timeout: 15000 }
          )
          .then((resposta) => {
            definirTokens(resposta.data)
            return resposta.data.accessToken
          })
          .finally(() => {
            renovacaoEmAndamento = null
          })
      }

      const novoToken = await renovacaoEmAndamento

      configuracao.headers.Authorization = `Bearer ${novoToken}`

      return api(configuracao)
    } catch (falhaRenovacao) {
      limparTokens()
      return Promise.reject(falhaRenovacao)
    }
  }
)

export async function autenticar(usuario, senha) {
  const resposta = await api.post("/api/auth/login", { login: usuario, senha })
  return resposta.data
}

export async function encerrarSessao(tokenRenovacao) {
  if (!tokenRenovacao) {
    return
  }

  await api.post("/api/auth/logout", { refreshToken: tokenRenovacao })
}

export async function listarBebidas() {
  const resposta = await api.get("/api/bebidas")
  return resposta.data
}

export async function cadastrarBebida(bebida) {
  const resposta = await api.post("/api/bebidas", bebida)
  return resposta.data
}

export async function venderUnidade(id) {
  const resposta = await api.post(`/api/bebidas/${id}/venda`, { quantidade: 1 })
  return resposta.data
}

export async function reporEstoque(id, quantidade) {
  const resposta = await api.post(`/api/bebidas/${id}/reposicao`, { quantidade })
  return resposta.data
}

export default api
