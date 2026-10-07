-- RN-14: numeração sequencial por competência (sem lacunas: participa da transação)
CREATE TABLE lancamento_sequencia (
    competencia VARCHAR(7) PRIMARY KEY,
    ultimo      INT NOT NULL
);

-- RNF-08: idempotência em endpoints de escrita (header Idempotency-Key)
CREATE TABLE idempotencia (
    chave      VARCHAR(100) NOT NULL,
    usuario_id UUID         NOT NULL,
    metodo     VARCHAR(10)  NOT NULL,
    caminho    VARCHAR(200) NOT NULL,
    status     INT          NOT NULL,
    corpo      TEXT,
    criado_em  TIMESTAMPTZ  NOT NULL,
    PRIMARY KEY (chave, usuario_id)
);
