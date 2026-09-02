package com.teste.miniautorizador.service.impl;

import com.teste.miniautorizador.dto.CartaoRequestDTO;
import com.teste.miniautorizador.dto.CartaoResponseDTO;
import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.exception.CartaoExistenteException;
import com.teste.miniautorizador.exception.CartaoInexistenteException;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.teste.miniautorizador.repository.CartaoRepository;
import com.teste.miniautorizador.validation.RegraAutorizacao;
import com.teste.miniautorizador.validation.RegraSaldoSuficiente;
import com.teste.miniautorizador.validation.RegraSenhaValida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Para evitar UnnecessaryStubbingException
class CartaoServiceImplTest {

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CartaoServiceImpl cartaoService;
    private List<RegraAutorizacao> regrasAutorizacao;

    private Cartao cartao;
    private CartaoRequestDTO cartaoRequest;
    private TransacaoRequestDTO transacaoRequest;

    @BeforeEach
    void setUp() {
        // Criar regras reais com mocks
        RegraSenhaValida regraSenhaValida = new RegraSenhaValida(passwordEncoder);
        RegraSaldoSuficiente regraSaldoSuficiente = new RegraSaldoSuficiente();
        regrasAutorizacao = Arrays.asList(regraSenhaValida, regraSaldoSuficiente);

        // Criar service manualmente
        cartaoService = new CartaoServiceImpl(
                cartaoRepository,
                regrasAutorizacao,
                passwordEncoder
        );

        cartao = Cartao.builder()
                .id(1L)
                .numeroCartao("6549873025634501")
                .senha("$2a$10$hashedPassword1234567890123456789012345678901234567890")
                .saldo(new BigDecimal("500.00"))
                .build();

        cartaoRequest = CartaoRequestDTO.builder()
                .numeroCartao("6549873025634501")
                .senha("1234")
                .build();

        transacaoRequest = TransacaoRequestDTO.builder()
                .numeroCartao("6549873025634501")
                .senhaCartao("1234")
                .valor(new BigDecimal("100.00"))
                .build();
    }

    @Test
    @DisplayName("Deve criar cartão com sucesso")
    void deveCriarCartaoComSucesso() {
        // Arrange
        when(cartaoRepository.findByNumeroCartao(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("$2a$10$hashedPassword");
        when(cartaoRepository.save(any(Cartao.class)))
                .thenAnswer(invocation -> {
                    Cartao cartaoSalvo = invocation.getArgument(0);
                    if (cartaoSalvo.getId() == null) {
                        cartaoSalvo.setId(1L);
                    }
                    return cartaoSalvo;
                });

        // Act
        CartaoResponseDTO response = cartaoService.criarCartao(cartaoRequest);

        // Assert
        assertNotNull(response);
        assertEquals("6549873025634501", response.getNumeroCartao());
        assertEquals("1234", response.getSenha());

        verify(cartaoRepository).save(any(Cartao.class));
        verify(passwordEncoder).encode("1234");
    }

    @Test
    @DisplayName("Deve lançar exceção quando cartão já existe")
    void deveLancarExcecaoQuandoCartaoExiste() {
        // Arrange
        when(cartaoRepository.findByNumeroCartao(anyString()))
                .thenReturn(Optional.of(cartao));

        // Act & Assert
        CartaoExistenteException exception = assertThrows(
                CartaoExistenteException.class,
                () -> cartaoService.criarCartao(cartaoRequest)
        );

        assertNotNull(exception.getCartaoResponse());
        verify(cartaoRepository, never()).save(any(Cartao.class));
    }

    @Test
    @DisplayName("Deve consultar saldo com sucesso")
    void deveConsultarSaldoComSucesso() {
        // Arrange
        when(cartaoRepository.findByNumeroCartao(anyString()))
                .thenReturn(Optional.of(cartao));

        // Act
        BigDecimal saldo = cartaoService.consultarSaldo("6549873025634501");

        // Assert
        assertEquals(new BigDecimal("500.00"), saldo);
    }

    @Test
    @DisplayName("Deve lançar exceção ao consultar cartão inexistente")
    void deveLancarExcecaoAoConsultarCartaoInexistente() {
        // Arrange
        when(cartaoRepository.findByNumeroCartao(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CartaoInexistenteException.class, () -> {
            cartaoService.consultarSaldo("9999999999999999");
        });
    }

    @Test
    @DisplayName("Deve autorizar transação com sucesso")
    void deveAutorizarTransacaoComSucesso() {
        // Arrange
        when(cartaoRepository.findByNumeroCartaoWithLock(anyString()))
                .thenReturn(Optional.of(cartao));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        // Act
        cartaoService.autorizarTransacao(transacaoRequest);

        // Assert
        assertEquals(new BigDecimal("400.00"), cartao.getSaldo());
        verify(cartaoRepository).save(cartao);
    }

    @Test
    @DisplayName("Deve recusar transação com senha inválida")
    void deveRecusarTransacaoComSenhaInvalida() {
        // Arrange
        when(cartaoRepository.findByNumeroCartaoWithLock(anyString()))
                .thenReturn(Optional.of(cartao));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        // Act & Assert
        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> cartaoService.autorizarTransacao(transacaoRequest)
        );

        assertEquals("SENHA_INVALIDA", exception.getMessage());
        verify(cartaoRepository, never()).save(any(Cartao.class));
    }

    @Test
    @DisplayName("Deve recusar transação com saldo insuficiente")
    void deveRecusarTransacaoComSaldoInsuficiente() {
        // Arrange
        Cartao cartaoPobre = Cartao.builder()
                .id(1L)
                .numeroCartao("6549873025634501")
                .senha("$2a$10$hashedPassword")
                .saldo(new BigDecimal("10.00"))
                .build();

        when(cartaoRepository.findByNumeroCartaoWithLock(anyString()))
                .thenReturn(Optional.of(cartaoPobre));
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);

        TransacaoRequestDTO transacaoGrande = TransacaoRequestDTO.builder()
                .numeroCartao("6549873025634501")
                .senhaCartao("1234")
                .valor(new BigDecimal("100.00"))
                .build();

        // Act & Assert
        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> cartaoService.autorizarTransacao(transacaoGrande)
        );

        assertEquals("SALDO_INSUFICIENTE", exception.getMessage());
        verify(cartaoRepository, never()).save(any(Cartao.class));
    }

    @Test
    @DisplayName("Deve recusar transação para cartão inexistente")
    void deveRecusarTransacaoParaCartaoInexistente() {
        // Arrange
        when(cartaoRepository.findByNumeroCartaoWithLock(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        TransacaoNaoAutorizadaException exception = assertThrows(
                TransacaoNaoAutorizadaException.class,
                () -> cartaoService.autorizarTransacao(transacaoRequest)
        );

        assertEquals("CARTAO_INEXISTENTE", exception.getMessage());
        verify(cartaoRepository, never()).save(any(Cartao.class));
    }
}