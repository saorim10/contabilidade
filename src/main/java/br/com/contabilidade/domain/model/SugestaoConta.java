package br.com.contabilidade.domain.model;

import java.util.UUID;

/** Sugestão não autoritativa de conta (origem: IA_SUGESTAO). Nunca é persistida. */
public record SugestaoConta(UUID contaId, String codigo, String nome, double confianca) {}
