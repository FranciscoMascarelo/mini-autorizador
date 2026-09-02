package com.teste.miniautorizador.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.miniautorizador.dto.CartaoRequestDTO;
import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FluxoCompletoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartaoRepository cartaoRepository;

    @BeforeEach
    void setUp() {
        cartaoRepository.deleteAll();
    }

    @Test
    @DisplayName("Fluxo completo: criar cartão, consultar saldo, realizar transações")
    void fluxoCompleto() throws Exception {
        // 1. Criar cartão
        CartaoRequestDTO cartaoRequest = new CartaoRequestDTO(
                "6549873025634501",
                "1234"
        );

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
                .andExpect(jsonPath("$.senha").value("1234"));

        // 2. Verificar saldo inicial
        mockMvc.perform(get("/cartoes/6549873025634501"))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));

        // 3. Realizar transação de R$ 10,00
        TransacaoRequestDTO transacao1 = new TransacaoRequestDTO(
                "6549873025634501",
                "1234",
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacao1)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));

        // 4. Verificar saldo após transação
        mockMvc.perform(get("/cartoes/6549873025634501"))
                .andExpect(status().isOk())
                .andExpect(content().string("490.00"));

        // 5. Verificar senha criptografada no banco
        Cartao cartaoNoBanco = cartaoRepository.findByNumeroCartao("6549873025634501")
                .orElseThrow();

        assertNotEquals("1234", cartaoNoBanco.getSenha(),
                "Senha deve estar criptografada no banco");
        assertTrue(cartaoNoBanco.getSenha().startsWith("$2a$"),
                "Senha deve usar BCrypt");
    }

    @Test
    @DisplayName("Fluxo com múltiplas transações até saldo insuficiente")
    void fluxoComMultiplasTransacoes() throws Exception {
        // Criar cartão
        CartaoRequestDTO cartaoRequest = new CartaoRequestDTO(
                "6549873025634501",
                "1234"
        );

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated());

        // Realizar transações até esgotar o saldo
        for (int i = 0; i < 50; i++) {
            TransacaoRequestDTO transacao = new TransacaoRequestDTO(
                    "6549873025634501",
                    "1234",
                    new BigDecimal("10.00")
            );

            mockMvc.perform(post("/transacoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(transacao)))
                    .andExpect(status().isCreated())
                    .andExpect(content().string("OK"));
        }

        // 51ª transação deve falhar
        TransacaoRequestDTO transacaoFalha = new TransacaoRequestDTO(
                "6549873025634501",
                "1234",
                new BigDecimal("10.00")
        );

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoFalha)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));

        // Saldo final deve ser zero
        mockMvc.perform(get("/cartoes/6549873025634501"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.00"));
    }
}