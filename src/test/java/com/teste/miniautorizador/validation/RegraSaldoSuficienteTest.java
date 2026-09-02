package com.teste.miniautorizador.validation;

import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class RegraSaldoSuficienteTest {

    private RegraSaldoSuficiente regraSaldoSuficiente;
    private Cartao cartao;
    private TransacaoRequestDTO transacao;

    @BeforeEach
    void setUp() {
        regraSaldoSuficiente = new RegraSaldoSuficiente();

        cartao = Cartao.builder()
                .id(1L)
                .numeroCartao("6549873025634501")
                .senha("$2a$10$hashedPassword")
                .saldo(new BigDecimal("500.00"))
                .build();

        transacao = TransacaoRequestDTO.builder()
                .numeroCartao("6549873025634501")
                .senhaCartao("1234")
                .valor(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Deve validar quando saldo é suficiente")
    void deveValidarQuandoSaldoSuficiente() {
        // Arrange
        cartao.setSaldo(new BigDecimal("500.00"));
        transacao.setValor(new BigDecimal("100.00"));

        // Act & Assert
        assertDoesNotThrow(() -> regraSaldoSuficiente.validar(cartao, transacao));
    }

    @Test
    @DisplayName("Deve validar quando saldo é exatamente igual ao valor da transação")
    void deveValidarQuandoSaldoIgual() {
        // Arrange
        cartao.setSaldo(new BigDecimal("100.00"));
        transacao.setValor(new BigDecimal("100.00"));

        // Act & Assert
        assertDoesNotThrow(() -> regraSaldoSuficiente.validar(cartao, transacao));
    }

    @Test
    @DisplayName("Deve lançar exceção quando saldo é insuficiente")
    void deveLancarExcecaoQuandoSaldoInsuficiente() {
        // Arrange
        cartao.setSaldo(new BigDecimal("50.00"));
        transacao.setValor(new BigDecimal("100.00"));

        // Act & Assert
        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> regraSaldoSuficiente.validar(cartao, transacao)
        );

        assertEquals("SALDO_INSUFICIENTE", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar ordem 2")
    void deveRetornarOrdem2() {
        assertEquals(2, regraSaldoSuficiente.getOrdem());
    }
}