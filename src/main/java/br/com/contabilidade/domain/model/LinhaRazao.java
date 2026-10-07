package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.enums.TipoPartida;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LinhaRazao(LocalDate data, String numero, String historico, TipoPartida tipo, BigDecimal valor) {}
