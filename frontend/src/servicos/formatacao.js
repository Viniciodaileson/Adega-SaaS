const formatadorMoeda = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL"
})

const formatadorNumero = new Intl.NumberFormat("pt-BR")

export function formatarMoeda(valor) {
  return formatadorMoeda.format(Number(valor ?? 0))
}

export function formatarQuantidade(valor) {
  return formatadorNumero.format(Number(valor ?? 0))
}
