# Sistema de Contabilidade — Backend

Motor contábil com partidas dobradas (ITG 2000 / CPC), implementado conforme a documentação do projeto:
Java 21, Spring Boot 3.2.3, PostgreSQL 16, Flyway, JWT, DDD + Clean Architecture.

## Como rodar

```bash
# tudo em containers (banco + app)
docker compose up --build

# ou só o banco em container e a app local
docker compose up -d db
mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Login: `POST /api/v1/auth/login` com `{"username":"admin","senha":"trocar-123"}`
- Usuários de desenvolvimento (criados se `SEED_USUARIOS=true`): `admin` (ADMIN), `senior` (CONTADOR_SENIOR), `contador` (CONTADOR), `auditor` (AUDITOR). **Desative em produção** e defina `JWT_SECRET`.
- Um plano de contas de exemplo é criado pela migration `V3`.

## Testes

```bash
mvn test      # unitários + integração (a integração exige Docker, via Testcontainers)
mvn verify    # inclui a verificação de cobertura >= 80% no pacote domain (JaCoCo)
```

## Estrutura

```
domain/          enums, vo (Dinheiro, Competencia, CodigoConta, NumeroLancamento), model (agregados),
                 event (eventos de domínio), repository (portas), exception
application/     usecase (CriarLancamento, Estornar, FecharPeriodo, ...), relatorio (balancete, balanço, DRE, razão), port
infrastructure/  persistence (JPA + adapters), security (JWT), event (publisher/outbox), ia, idempotency, config
presentation/    controller, dto, error (RFC 7807)
```

## Endpoints (`/api/v1`)

Os da documentação, mais: `POST /periodos/{AAAA-MM}/abrir|bloquear|desbloquear|reabrir` (RF-04).
Relatórios recebem a competência por query string: `/relatorios/balancete?competencia=2025-01`,
`/relatorios/dre?competenciaInicial=2025-01&competenciaFinal=2025-03`, `/relatorios/razao/{contaId}?competencia=2025-01`.

Perfis: ADMIN tudo; CONTADOR_SENIOR escrita + fechar/bloquear período; CONTADOR escrita; AUDITOR leitura + auditoria; só ADMIN reabre período.

## Decisões e simplificações (revise se divergirem da sua intenção)

- **Período inexistente** é aberto automaticamente no primeiro lançamento daquela competência.
- **Conta sintética** = conta que possui filhas (derivada do código hierárquico; não há coluna extra). A conta pai é resolvida pelo código (`1.1.01` → `1.1`).
- **RN-15** foi interpretada como "sem lançamentos em RASCUNHO" (não existe conceito de conciliação na documentação).
- **Estorno** é datado de hoje (fuso America/Sao_Paulo); o período atual precisa estar aberto.
- **RN-14**: numeração via tabela `lancamento_sequencia` (UPSERT atômico, sem lacunas).
- **RN-13**: além do domínio, triggers no banco impedem alterar/excluir lançamentos (só o status muda) e partidas/eventos.
- **Eventos**: gravados em `evento_auditoria` na mesma transação (outbox) e publicados via `ApplicationEventPublisher`; Kafka/RabbitMQ fica para depois.
- **IA**: `SugestorPorPalavrasChave` é um stub por correspondência de palavras; troque por um LLM implementando a porta `SugestorDeConta`.
- **Balanço**: soma o resultado acumulado ao lado Passivo + PL (não há apuração anual de resultado).
- `PlanoContas` não é uma classe separada: `Conta` é o agregado.
- Idempotência (RNF-08) via filtro + tabela `idempotencia`; requisições concorrentes com a mesma chave não são serializadas.

## Não implementado

RF-11 (importação CSV), frontend Angular 17 e o item "Roadmap" (não veio no texto da documentação).
