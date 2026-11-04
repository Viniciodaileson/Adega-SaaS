package com.adega.security.refresh;

public record RotacaoRefreshToken(Long usuarioId, String novoToken) {
}
