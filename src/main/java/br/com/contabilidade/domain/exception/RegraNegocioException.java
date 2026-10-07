package br.com.contabilidade.domain.exception;

/** Violação de regra de negócio (RN-xx). Mapeada para HTTP 422 na camada de apresentação. */
public class RegraNegocioException extends RuntimeException {
    private final String regra;

    public RegraNegocioException(String regra, String mensagem) {
        super(mensagem);
        this.regra = regra;
    }

    public String getRegra() { return regra; }
}
