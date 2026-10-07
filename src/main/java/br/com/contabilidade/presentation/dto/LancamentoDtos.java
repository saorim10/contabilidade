package br.com.contabilidade.presentation.dto;

import br.com.contabilidade.domain.enums.OrigemLancamento;
import br.com.contabilidade.domain.enums.StatusLancamento;
import br.com.contabilidade.domain.enums.TipoPartida;
import br.com.contabilidade.domain.model.LancamentoContabil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class LancamentoDtos {
    private LancamentoDtos() {}

    public record PartidaRequest(@NotNull UUID contaId, @NotNull TipoPartida tipo, @NotNull BigDecimal valor) {}

    public record CriarLancamentoRequest(@NotNull LocalDate data, @NotBlank String historico, OrigemLancamento origem,
                                         @NotEmpty List<@Valid @NotNull PartidaRequest> partidas) {}

    public record EstornoRequest(@NotBlank String motivo) {}

    /** Resposta 201 conforme a documentação (seção 9.3). */
    public record LancamentoCriadoResponse(UUID id, String numero, StatusLancamento status, Instant criadoEm) {
        public static LancamentoCriadoResponse de(LancamentoContabil l) {
            return new LancamentoCriadoResponse(l.getId(), l.getNumero().toString(), l.getStatus(), l.getCriadoEm());
        }
    }

    public record PartidaResponse(UUID id, UUID contaId, TipoPartida tipo, BigDecimal valor) {}

    public record LancamentoDetalheResponse(UUID id, String numero, LocalDate data, String competencia, String historico,
                                            StatusLancamento status, OrigemLancamento origem, UUID criadoPor, Instant criadoEm,
                                            UUID estornoDeId, List<PartidaResponse> partidas) {
        public static LancamentoDetalheResponse de(LancamentoContabil l) {
            return new LancamentoDetalheResponse(l.getId(), l.getNumero().toString(), l.getData(), l.getCompetencia().toString(),
                    l.getHistorico(), l.getStatus(), l.getOrigem(), l.getCriadoPor(), l.getCriadoEm(), l.getEstornoDeId(),
                    l.getPartidas().stream().map(p -> new PartidaResponse(p.id(), p.contaId(), p.tipo(), p.valor().valor())).toList());
        }
    }
}
