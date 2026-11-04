package com.adega.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "adega.jwt")
public class JwtProperties {

    private String emissor = "adega-saas";
    private long tempoVidaMinutos = 15;
    private long tempoVidaRefreshDias = 7;

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
}
