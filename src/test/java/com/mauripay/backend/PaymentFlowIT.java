package com.mauripay.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mauripay.backend.webhook.WebhookDeliveryRepository;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PaymentFlowIT {

    private static final String API_KEY = "dev-merchant-key-please-change";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    WebhookDeliveryRepository webhookRepository;

    @Test
    void fullPayByCodeFlow() throws Exception {
        // register + login the paying user
        register("22200001", "Alice");
        MockHttpSession session = login("22200001");

        // fund the account (dev helper)
        mvc.perform(post("/api/v1/dev/topup").session(session)
                        .contentType(APPLICATION_JSON).content("{\"amount\":500.00}"))
                .andExpect(status().isOk());

        // merchant generates a code for a fixed amount + webhook url
        String code = createPayment("120.00", "https://example.invalid/webhook");

        // user previews -> sees the uneditable amount
        mvc.perform(get("/api/v1/payments/" + code).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(120.00))
                .andExpect(jsonPath("$.merchantName").value("Demo Merchant"));

        // wrong password -> 401
        mvc.perform(post("/api/v1/payments/" + code + "/pay").session(session)
                        .contentType(APPLICATION_JSON).content("{\"password\":\"wrongpass\"}"))
                .andExpect(status().isUnauthorized());

        // user pays with correct password -> PAID
        mvc.perform(post("/api/v1/payments/" + code + "/pay").session(session)
                        .contentType(APPLICATION_JSON).content("{\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        // balance debited by exactly the fixed amount
        mvc.perform(get("/api/v1/me").session(session))
                .andExpect(jsonPath("$.balance").value(380.00));

        // a webhook delivery was enqueued
        assertThat(webhookRepository.findAll()).hasSize(1);

        // paying again is idempotent -> 409
        mvc.perform(post("/api/v1/payments/" + code + "/pay").session(session)
                        .contentType(APPLICATION_JSON).content("{\"password\":\"secret123\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void insufficientBalanceIsRejected() throws Exception {
        register("22200002", "Bob");
        MockHttpSession session = login("22200002");
        String code = createPayment("999.00", null);
        mvc.perform(post("/api/v1/payments/" + code + "/pay").session(session)
                        .contentType(APPLICATION_JSON).content("{\"password\":\"secret123\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    private void register(String phone, String name) throws Exception {
        mvc.perform(post("/api/v1/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"fullName\":\"" + name
                                + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isCreated());
    }

    private MockHttpSession login(String phone) throws Exception {
        MvcResult result = mvc.perform(post("/api/v1/auth/login").contentType(APPLICATION_JSON)
                        .content("{\"phone\":\"" + phone + "\",\"password\":\"secret123\"}"))
                .andExpect(status().isOk())
                .andReturn();
        HttpSession session = result.getRequest().getSession(false);
        return (MockHttpSession) session;
    }

    private String createPayment(String amount, String callbackUrl) throws Exception {
        String body = callbackUrl == null
                ? "{\"amount\":" + amount + ",\"currency\":\"MRU\"}"
                : "{\"amount\":" + amount + ",\"currency\":\"MRU\",\"callbackUrl\":\"" + callbackUrl + "\"}";
        MvcResult result = mvc.perform(post("/api/v1/payments")
                        .header("X-Api-Key", API_KEY)
                        .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = mapper.readTree(result.getResponse().getContentAsString());
        return node.get("code").asText();
    }
}
