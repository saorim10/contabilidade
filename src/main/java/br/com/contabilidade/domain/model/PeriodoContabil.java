package br.com.contabilidade.domain.model;

import br.com.contabilidade.domain.enums.StatusPeriodo;
import br.com.contabilidade.domain.event.PeriodoFechado;
import br.com.contabilidade.domain.exception.RegraNegocioException;
import br.com.contabilidade.domain.vo.Competencia;
import java.time.Instant;
import java.util.UUID;

public class PeriodoContabil extends AggregateRoot {
    private final UUID id;
    private final Competencia competencia;
    private StatusPeriodo status;
    private Instant fechadoEm;
    private UUID fechadoPor;

    private PeriodoContabil(UUID id, Competencia competencia, StatusPeriodo status, Instant fechadoEm, UUID fechadoPor) {
        this.id = id; this.competencia = competencia; this.status = status; this.fechadoEm = fechadoEm; this.fechadoPor = fechadoPor;
    }

    public static PeriodoContabil abrir(Competencia competencia) {
        return new PeriodoContabil(UUID.randomUUID(), competencia, StatusPeriodo.ABERTO, null, null);
    }

    public static PeriodoContabil reconstituir(UUID id, Competencia competencia, StatusPeriodo status, Instant fechadoEm, UUID fechadoPor) {
        return new PeriodoContabil(id, competencia, status, fechadoEm, fechadoPor);
    }

    /** ABERTO -> FECHADO */
    public void fechar(UUID usuarioId) {
        exigir(StatusPeriodo.ABERTO, "fechar");
        status = StatusPeriodo.FECHADO;
        fechadoEm = Instant.now();
        fechadoPor = usuarioId;
        registrar(new PeriodoFechado(id, competencia.toString(), usuarioId, fechadoEm));
    }

    /** FECHADO -> BLOQUEADO */
    public void bloquear() {
        exigir(StatusPeriodo.FECHADO, "bloquear");
        status = StatusPeriodo.BLOQUEADO;
    }

    /** BLOQUEADO -> FECHADO */
    public void desbloquear() {
        exigir(StatusPeriodo.BLOQUEADO, "desbloquear");
        status = StatusPeriodo.FECHADO;
    }

    /** FECHADO -> ABERTO (perfil ADMIN, verificado na camada de segurança) */
    public void reabrir() {
        exigir(StatusPeriodo.FECHADO, "reabrir");
        status = StatusPeriodo.ABERTO;
        fechadoEm = null;
        fechadoPor = null;
    }

    public boolean aceitaLancamento() { return status == StatusPeriodo.ABERTO; }

    private void exigir(StatusPeriodo esperado, String acao) {
        if (status != esperado) {
            throw new RegraNegocioException("PERIODO-01",
                    "Não é possível " + acao + " o período " + competencia + ": status atual é " + status + " (esperado " + esperado + ")");
        }
    }

    public UUID getId() { return id; }
    public Competencia getCompetencia() { return competencia; }
    public StatusPeriodo getStatus() { return status; }
    public Instant getFechadoEm() { return fechadoEm; }
    public UUID getFechadoPor() { return fechadoPor; }
}
