package br.com.contabilidade.domain.vo;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/** Código hierárquico de conta, ex.: "1.1.01". */
public record CodigoConta(String valor) {
    private static final Pattern FORMATO = Pattern.compile("^\\d+(\\.\\d+)*$");

    public CodigoConta {
        Objects.requireNonNull(valor, "codigo");
        valor = valor.trim();
        if (!FORMATO.matcher(valor).matches()) {
            throw new IllegalArgumentException("Código de conta inválido (ex.: 1.1.01): " + valor);
        }
    }

    public Optional<CodigoConta> pai() {
        int i = valor.lastIndexOf('.');
        return i < 0 ? Optional.empty() : Optional.of(new CodigoConta(valor.substring(0, i)));
    }

    public boolean isFilha() { return valor.contains("."); }

    @Override public String toString() { return valor; }
}
