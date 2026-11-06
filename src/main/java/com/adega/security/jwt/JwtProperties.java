package com.adega.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "adega.jwt")
public class JwtProperties {

    private String emissor = "adega-saas";
    private long tempoVidaMinutos = 15;
    private long tempoVidaRefreshDias = 7;

    /**
     * JWK privado em JSON (com os campos x, y, d). Tem prioridade sobre o arquivo.
     * Indicado para produção, injetado por variável de ambiente ou cofre.
     */
    private String chaveJwk = "";

    /**
     * Arquivo local onde a chave é carregada/gravada. Deixe vazio para gerar
     * uma chave efêmera (apenas para testes).
     */
    private String arquivoChave = "adega-jwt.jwk";

    public String getEmissor() {
        return emissor;
    }

    public void setEmissor(String emissor) {
        this.emissor = emissor;
    }

    public long getTempoVidaMinutos() {
        return tempoVidaMinutos;
    }

    public void setTempoVidaMinutos(long tempoVidaMinutos) {
        this.tempoVidaMinutos = tempoVidaMinutos;
    }

    public long getTempoVidaRefreshDias() {
        return tempoVidaRefreshDias;
    }

    public void setTempoVidaRefreshDias(long tempoVidaRefreshDias) {
        this.tempoVidaRefreshDias = tempoVidaRefreshDias;
    }

    public String getChaveJwk() {
        return chaveJwk;
    }

    public void setChaveJwk(String chaveJwk) {
        this.chaveJwk = chaveJwk;
    }

    public String getArquivoChave() {
        return arquivoChave;
    }

    public void setArquivoChave(String arquivoChave) {
        this.arquivoChave = arquivoChave;
    }
}
