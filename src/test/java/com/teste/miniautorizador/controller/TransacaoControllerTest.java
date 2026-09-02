package com.teste.miniautorizador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.teste.miniautorizador.service.CartaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransacaoController.class)
class TransacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartaoService cartaoService;

    private TransacaoRequestDTO transacaoRequest;

    @BeforeEach
    void setUp() {
        transacaoRequest = new TransacaoRequestDTO(
                "6549873025634501",
                "1234",
                new BigDecimal("10.00")
        );
    }

    @Test
    @DisplayName("POST /transacoes - Deve autorizar transação com sucesso")
    void deveAutorizarTransacaoComSucesso() throws Exception {
        doNothing().when(cartaoService).autorizarTransacao(any(TransacaoRequestDTO.class));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("OK"));
    }

    @Test
    @DisplayName("POST /transacoes - Deve retornar 422 para senha inválida")
    void deveRetornar422ParaSenhaInvalida() throws Exception {
        doThrow(new TransacaoNaoAutorizadaException("SENHA_INVALIDA"))
                .when(cartaoService).autorizarTransacao(any(TransacaoRequestDTO.class));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SENHA_INVALIDA"));
    }

    @Test
    @DisplayName("POST /transacoes - Deve retornar 422 para saldo insuficiente")
    void deveRetornar422ParaSaldoInsuficiente() throws Exception {
        doThrow(new TransacaoNaoAutorizadaException("SALDO_INSUFICIENTE"))
                .when(cartaoService).autorizarTransacao(any(TransacaoRequestDTO.class));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("SALDO_INSUFICIENTE"));
    }

    @Test
    @DisplayName("POST /transacoes - Deve retornar 422 para cartão inexistente")
    void deveRetornar422ParaCartaoInexistente() throws Exception {
        doThrow(new TransacaoNaoAutorizadaException("CARTAO_INEXISTENTE"))
                .when(cartaoService).autorizarTransacao(any(TransacaoRequestDTO.class));

        mockMvc.perform(post("/transacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transacaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().string("CARTAO_INEXISTENTE"));
    }
}