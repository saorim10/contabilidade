package br.com.contabilidade.domain.repository;

import br.com.contabilidade.domain.vo.Competencia;
import br.com.contabilidade.domain.vo.NumeroLancamento;

public interface GeradorNumeroLancamento {
    /** Próximo número sequencial da competência (RN-14). */
    NumeroLancamento proximo(Competencia competencia);
}
