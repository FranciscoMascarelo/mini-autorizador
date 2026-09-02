package com.teste.miniautorizador.service;

import com.teste.miniautorizador.dto.CartaoRequestDTO;
import com.teste.miniautorizador.dto.CartaoResponseDTO;
import com.teste.miniautorizador.dto.TransacaoRequestDTO;

import java.math.BigDecimal;

public interface CartaoService {
    CartaoResponseDTO criarCartao(CartaoRequestDTO request);
    BigDecimal consultarSaldo(String numeroCartao);
    void autorizarTransacao(TransacaoRequestDTO transacao);
}
