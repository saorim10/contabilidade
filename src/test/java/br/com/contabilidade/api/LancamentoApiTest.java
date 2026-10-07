package br.com.contabilidade.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/** RNF-04: teste de integração com PostgreSQL real (Testcontainers). Requer Docker. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class LancamentoApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mvc;

    static String token;

    String login(String usuario) throws Exception {
        var body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + usuario + "\",\"senha\":\"trocar-123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    String contaId(String t, String codigo) throws Exception {
        var body = mvc.perform(get("/api/v1/contas").header("Authorization", "Bearer " + t)).andReturn().getResponse().getContentAsString();
        List<String> ids = JsonPath.read(body, "$[?(@.codigo=='" + codigo + "')].id");
        return ids.get(0);
    }

    String lancamentoJson(String data, String debito, String credito, String t) throws Exception {
        return """
            {"data":"%s","historico":"Pagamento de fornecedor XYZ","origem":"MANUAL","partidas":[
              {"contaId":"%s","tipo":"DEBITO","valor":%s},
              {"contaId":"%s","tipo":"CREDITO","valor":%s}]}
            """.formatted(data, contaId(t, "2.1.01"), debito, contaId(t, "1.1.02"), credito);
    }

    @Test
    void naoAutenticadoRecebe401EmProblemJson() throws Exception {
        mvc.perform(get("/api/v1/contas")).andExpect(status().isUnauthorized());
    }

    @Test
    void criaLancamentoEEstorna() throws Exception {
        var t = login("contador");
        var resposta = mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + t)
                        .contentType(MediaType.APPLICATION_JSON).content(lancamentoJson("2025-01-15", "1500.00", "1500.00", t)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.numero").value(org.hamcrest.Matchers.matchesPattern("2025-01-\\d{6}")))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(resposta, "$.id");

        mvc.perform(post("/api/v1/lancamentos/" + id + "/estorno").header("Authorization", "Bearer " + t)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"motivo\":\"Lançado em duplicidade\"}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/lancamentos/" + id).header("Authorization", "Bearer " + t))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ESTORNADO"));
    }

    @Test
    void rejeitaLancamentoDesbalanceadoCom422() throws Exception {
        var t = login("contador");
        mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + t)
                        .contentType(MediaType.APPLICATION_JSON).content(lancamentoJson("2025-01-16", "100.00", "99.00", t)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.regra").value("RN-01"));
    }

    @Test
    void periodoFechadoNaoAceitaLancamentos() throws Exception {
        var senior = login("senior");
        mvc.perform(post("/api/v1/periodos/2025-02/abrir").header("Authorization", "Bearer " + senior)).andExpect(status().isOk());
        mvc.perform(post("/api/v1/periodos/2025-02/fechar").header("Authorization", "Bearer " + senior))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FECHADO"));

        mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + senior)
                        .contentType(MediaType.APPLICATION_JSON).content(lancamentoJson("2025-02-10", "10.00", "10.00", senior)))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.regra").value("RN-05"));
    }

    @Test
    void idempotencyKeyEvitaDuplicidade() throws Exception {
        var t = login("contador");
        var json = lancamentoJson("2025-03-10", "10.00", "10.00", t);
        var primeira = mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + t).header("Idempotency-Key", "chave-123")
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        var segunda = mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + t).header("Idempotency-Key", "chave-123")
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        assertThat((String) JsonPath.read(segunda, "$.id")).isEqualTo(JsonPath.read(primeira, "$.id"));
    }

    @Test
    void auditorNaoPodeCriarLancamentoMasLeAuditoria() throws Exception {
        var auditor = login("auditor");
        mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + auditor)
                        .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/auditoria/eventos").header("Authorization", "Bearer " + auditor)).andExpect(status().isOk());
    }

    @Test
    void contaComSaldoNaoPodeSerInativada() throws Exception {
        var t = login("admin");
        mvc.perform(post("/api/v1/lancamentos").header("Authorization", "Bearer " + t)
                        .contentType(MediaType.APPLICATION_JSON).content(lancamentoJson("2025-04-10", "75.00", "75.00", t))).andExpect(status().isCreated());
        mvc.perform(patch("/api/v1/contas/" + contaId(t, "1.1.02") + "/inativar").header("Authorization", "Bearer " + t))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.regra").value("RN-08"));
    }
}
