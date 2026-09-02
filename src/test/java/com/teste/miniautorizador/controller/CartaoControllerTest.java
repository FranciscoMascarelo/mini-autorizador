package com.teste.miniautorizador.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teste.miniautorizador.dto.CartaoRequestDTO;
import com.teste.miniautorizador.dto.CartaoResponseDTO;
import com.teste.miniautorizador.exception.CartaoExistenteException;
import com.teste.miniautorizador.exception.CartaoInexistenteException;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartaoController.class)
class CartaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartaoService cartaoService;

    private CartaoRequestDTO cartaoRequest;
    private CartaoResponseDTO cartaoResponse;

    @BeforeEach
    void setUp() {
        cartaoRequest = new CartaoRequestDTO(
                "6549873025634501",
                "1234"
        );

        cartaoResponse = new CartaoResponseDTO(
                "6549873025634501",
                "1234"
        );
    }

    @Test
    @DisplayName("POST /cartoes - Deve criar cartão com sucesso")
    void deveCriarCartaoComSucesso() throws Exception {
        when(cartaoService.criarCartao(any(CartaoRequestDTO.class)))
                .thenReturn(cartaoResponse);

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
                .andExpect(jsonPath("$.senha").value("1234"));
    }

    @Test
    @DisplayName("POST /cartoes - Deve retornar 422 quando cartão existe")
    void deveRetornar422QuandoCartaoExiste() throws Exception {
        when(cartaoService.criarCartao(any(CartaoRequestDTO.class)))
                .thenThrow(new CartaoExistenteException(cartaoResponse));

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartaoRequest)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.numeroCartao").value("6549873025634501"))
                .andExpect(jsonPath("$.senha").value("1234"));
    }

    @Test
    @DisplayName("POST /cartoes - Deve retornar 400 para dados inválidos")
    void deveRetornar400ParaDadosInvalidos() throws Exception {
        CartaoRequestDTO requestInvalido = new CartaoRequestDTO("", "");

        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestInvalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /cartoes/{numero} - Deve consultar saldo com sucesso")
    void deveConsultarSaldoComSucesso() throws Exception {
        when(cartaoService.consultarSaldo(anyString()))
                .thenReturn(new BigDecimal("500.00"));

        mockMvc.perform(get("/cartoes/6549873025634501"))
                .andExpect(status().isOk())
                .andExpect(content().string("500.00"));
    }

    @Test
    @DisplayName("GET /cartoes/{numero} - Deve retornar 404 para cartão inexistente")
    void deveRetornar404ParaCartaoInexistente() throws Exception {
        when(cartaoService.consultarSaldo(anyString()))
                .thenThrow(new CartaoInexistenteException("Cartão não encontrado"));

        mockMvc.perform(get("/cartoes/9999999999999999"))
                .andExpect(status().isNotFound());
    }
}