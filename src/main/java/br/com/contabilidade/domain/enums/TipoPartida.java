package br.com.contabilidade.domain.enums;

public enum TipoPartida {
    DEBITO, CREDITO;

    public TipoPartida inverso() { return this == DEBITO ? CREDITO : DEBITO; }
}
