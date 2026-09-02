package com.teste.miniautorizador.validation;

import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegraSenhaValidaTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private RegraSenhaValida regraSenhaValida;
    private Cartao cartao;
    private TransacaoRequestDTO transacao;

    @BeforeEach
    void setUp() {
        regraSenhaValida = new RegraSenhaValida(passwordEncoder);

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
    @DisplayName("Deve validar senha correta")
    void deveValidarSenhaCorreta() {
        // Arrange
        when(passwordEncoder.matches("1234", cartao.getSenha()))
                .thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> regraSenhaValida.validar(cartao, transacao));
        verify(passwordEncoder).matches("1234", cartao.getSenha());
    }

    @Test
    @DisplayName("Deve lançar exceção para senha incorreta")
    void deveLancarExcecaoParaSenhaIncorreta() {
        // Arrange
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // Act & Assert
        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> regraSenhaValida.validar(cartao, transacao)
        );

        assertEquals("SENHA_INVALIDA", exception.getMessage());
    }

    @Test
    @DisplayName("Deve retornar ordem 1")
    void deveRetornarOrdem1() {
        assertEquals(1, regraSenhaValida.getOrdem());
    }
}