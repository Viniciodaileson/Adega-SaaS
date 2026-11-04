let accessToken = null
let refreshToken = null

const ouvintes = new Set()

function notificar() {
  const ativo = possuiSessao()
  ouvintes.forEach((ouvinte) => ouvinte(ativo))
}

export function possuiSessao() {
  return Boolean(accessToken)
}

export function obterAccessToken() {
  return accessToken
}

export function obterRefreshToken() {
  return refreshToken
}

export function definirTokens(resposta) {
  accessToken = resposta?.accessToken ?? null
  refreshToken = resposta?.refreshToken ?? null
  notificar()
}

export function limparTokens() {
  accessToken = null
  refreshToken = null
  notificar()
}

export function inscreverOuvinte(ouvinte) {
  ouvintes.add(ouvinte)

  return () => {
    ouvintes.delete(ouvinte)
  }
}
