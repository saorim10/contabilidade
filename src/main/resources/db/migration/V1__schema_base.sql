-- Tabelas conforme seção 12 da documentação

CREATE TABLE conta (
    id            UUID PRIMARY KEY,
    codigo        VARCHAR(20)  NOT NULL UNIQUE,
    nome          VARCHAR(120) NOT NULL,
    tipo          VARCHAR(30)  NOT NULL,
    natureza      VARCHAR(10)  NOT NULL,
    conta_pai_id  UUID REFERENCES conta (id),
    ativa         BOOLEAN      NOT NULL DEFAULT true,
    criado_em     TIMESTAMPTZ  NOT NULL,
    atualizado_em TIMESTAMPTZ  NOT NULL,
    CONSTRAINT ck_conta_tipo CHECK (tipo IN ('ATIVO','PASSIVO','PATRIMONIO_LIQUIDO','RECEITA','DESPESA')),
    CONSTRAINT ck_conta_natureza CHECK (natureza IN ('DEVEDORA','CREDORA'))
);

CREATE TABLE periodo_contabil (
    id         UUID PRIMARY KEY,
    ano        INT         NOT NULL,
    mes        INT         NOT NULL CHECK (mes BETWEEN 1 AND 12),
    status     VARCHAR(20) NOT NULL CHECK (status IN ('ABERTO','FECHADO','BLOQUEADO')),
    fechado_em TIMESTAMPTZ,
    fechado_por UUID,
    UNIQUE (ano, mes)
);

CREATE TABLE lancamento (
    id            UUID PRIMARY KEY,
    numero        VARCHAR(30) NOT NULL UNIQUE,
    data          DATE        NOT NULL,
    competencia   VARCHAR(7)  NOT NULL,
    historico     TEXT        NOT NULL,
    status        VARCHAR(20) NOT NULL CHECK (status IN ('RASCUNHO','CONFIRMADO','ESTORNADO')),
    origem        VARCHAR(20) NOT NULL CHECK (origem IN ('MANUAL','IMPORTACAO','IA_SUGESTAO','ESTORNO')),
    criado_por    UUID        NOT NULL,
    criado_em     TIMESTAMPTZ NOT NULL,
    estorno_de_id UUID REFERENCES lancamento (id)
);

CREATE TABLE partida (
    id            UUID PRIMARY KEY,
    lancamento_id UUID           NOT NULL REFERENCES lancamento (id),
    conta_id      UUID           NOT NULL REFERENCES conta (id),
    tipo          VARCHAR(10)    NOT NULL CHECK (tipo IN ('DEBITO','CREDITO')),
    valor         NUMERIC(18, 2) NOT NULL CHECK (valor > 0)
);

CREATE TABLE evento_auditoria (
    id          UUID PRIMARY KEY,
    tipo_evento VARCHAR(60) NOT NULL,
    agregado_id UUID        NOT NULL,
    payload     JSONB       NOT NULL,
    ocorrido_em TIMESTAMPTZ NOT NULL,
    usuario_id  UUID
);

CREATE TABLE usuario (
    id         UUID PRIMARY KEY,
    username   VARCHAR(60)  NOT NULL UNIQUE,
    senha_hash VARCHAR(120) NOT NULL,
    papeis     VARCHAR(120) NOT NULL,
    ativo      BOOLEAN      NOT NULL DEFAULT true
);

-- Índices recomendados (12.7)
CREATE INDEX idx_lancamento_competencia_status ON lancamento (competencia, status);
CREATE INDEX idx_partida_conta_id ON partida (conta_id);
CREATE INDEX idx_partida_lancamento_id ON partida (lancamento_id);
CREATE INDEX idx_conta_codigo ON conta (codigo varchar_pattern_ops); -- b-tree com suporte a prefixo
CREATE INDEX idx_evento_agregado ON evento_auditoria (agregado_id);

-- RN-13: lançamentos imutáveis (apenas o status pode mudar) e trilha de auditoria append-only
CREATE FUNCTION proibir_alteracao() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Registro imutável (%): operação % não permitida', TG_TABLE_NAME, TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_partida_imutavel BEFORE UPDATE OR DELETE ON partida
    FOR EACH ROW EXECUTE FUNCTION proibir_alteracao();
CREATE TRIGGER trg_evento_imutavel BEFORE UPDATE OR DELETE ON evento_auditoria
    FOR EACH ROW EXECUTE FUNCTION proibir_alteracao();

CREATE FUNCTION proteger_lancamento() RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'Lançamentos são imutáveis (RN-13): exclusão não permitida';
    END IF;
    IF (NEW.id, NEW.numero, NEW.data, NEW.competencia, NEW.historico, NEW.origem, NEW.criado_por, NEW.criado_em, NEW.estorno_de_id)
       IS DISTINCT FROM
       (OLD.id, OLD.numero, OLD.data, OLD.competencia, OLD.historico, OLD.origem, OLD.criado_por, OLD.criado_em, OLD.estorno_de_id) THEN
        RAISE EXCEPTION 'Lançamentos são imutáveis (RN-13): apenas o status pode mudar';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_lancamento_imutavel BEFORE UPDATE OR DELETE ON lancamento
    FOR EACH ROW EXECUTE FUNCTION proteger_lancamento();
