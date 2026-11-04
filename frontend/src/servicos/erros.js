export function extrairMensagemErro(erro) {
  const resposta = erro?.response?.data

  if (Array.isArray(resposta?.detalhes) && resposta.detalhes.length > 0) {
    return resposta.detalhes.join(" ")
  }

  if (typeof resposta?.erro === "string" && resposta.erro.trim()) {
    return resposta.erro
  }

  if (erro?.code === "ECONNABORTED") {
    return "O servidor demorou para responder. Tente novamente."
  }

  if (!erro?.response) {
    return "Não foi possível conectar ao servidor. Verifique se a API está em execução."
  }

  return "Não foi possível concluir a operação. Tente novamente."
}
