package br.com.contabilidade.domain.enums;

public enum TipoConta {
    ATIVO(NaturezaSaldo.DEVEDORA),
    PASSIVO(NaturezaSaldo.CREDORA),
    PATRIMONIO_LIQUIDO(NaturezaSaldo.CREDORA),
    RECEITA(NaturezaSaldo.CREDORA),
    DESPESA(NaturezaSaldo.DEVEDORA);

    private final NaturezaSaldo naturezaPadrao;

    TipoConta(NaturezaSaldo naturezaPadrao) { this.naturezaPadrao = naturezaPadrao; }

    public NaturezaSaldo naturezaPadrao() { return naturezaPadrao; }
}
