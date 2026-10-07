package br.com.contabilidade.presentation.dto;

import br.com.contabilidade.domain.enums.NaturezaSaldo;
import br.com.contabilidade.domain.enums.TipoConta;
import br.com.contabilidade.domain.model.Conta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class ContaDtos {
    private ContaDtos() {}

    /** A conta pai é resolvida pelo código (ex.: 1.1.01 -> 1.1). Natureza é opcional (padrão do tipo). */
    public record CriarContaRequest(@NotBlank @Size(max = 20) String codigo, @NotBlank @Size(max = 120) String nome,
                                    @NotNull TipoConta tipo, NaturezaSaldo natureza) {}

    public record ContaResponse(UUID id, String codigo, String nome, TipoConta tipo, NaturezaSaldo natureza,
                                UUID contaPaiId, boolean ativa, Instant criadoEm, Instant atualizadoEm) {
        public static ContaResponse de(Conta c) {
            return new ContaResponse(c.getId(), c.getCodigo().valor(), c.getNome(), c.getTipo(), c.getNatureza(),
                    c.getContaPaiId(), c.isAtiva(), c.getCriadoEm(), c.getAtualizadoEm());
        }
    }
}
