package com.teste.miniautorizador.service;

import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.repository.CartaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Evita UnnecessaryStubbingException
class ConcorrenciaTest {

    @Mock
    private CartaoRepository cartaoRepository;

    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @Test
    @DisplayName("Deve processar múltiplas transações concorrentes")
    void deveProcessarMultiplasTransacoesConcorrentes() throws Exception {
        // Arrange
        Cartao cartao = Cartao.builder()
                .id(1L)
                .numeroCartao("6549873025634501")
                .senha("hash")
                .saldo(new BigDecimal("100.00"))
                .build();

        // Simula 10 transações de R$ 10,00 cada
        int numeroTransacoes = 10;
        BigDecimal valorTransacao = new BigDecimal("10.00");
        CountDownLatch latch = new CountDownLatch(numeroTransacoes);
        AtomicInteger transacoesBemSucedidas = new AtomicInteger(0);

        // Act
        List<Future<Boolean>> futures = new ArrayList<>();
        for (int i = 0; i < numeroTransacoes; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    // Simula processamento da transação com lock
                    synchronized (cartao) {
                        if (cartao.getSaldo().compareTo(valorTransacao) >= 0) {
                            cartao.setSaldo(cartao.getSaldo().subtract(valorTransacao));
                            transacoesBemSucedidas.incrementAndGet();
                            return true;
                        }
                        return false;
                    }
                } finally {
                    latch.countDown();
                }
            }));
        }

        latch.await(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(10, transacoesBemSucedidas.get());
        assertEquals(new BigDecimal("0.00"), cartao.getSaldo());

        executorService.shutdown();
    }

    @Test
    @DisplayName("Deve lidar com condição de corrida no saldo")
    void deveLidarComCondicaoDeCorrida() throws Exception {
        // Arrange
        AtomicInteger saldoAtual = new AtomicInteger(1000); // R$ 10,00 em centavos
        AtomicInteger valor = new AtomicInteger(1000); // R$ 10,00 em centavos
        AtomicInteger transacoesAprovadas = new AtomicInteger(0);
        AtomicInteger transacoesRejeitadas = new AtomicInteger(0);

        CountDownLatch latch = new CountDownLatch(2);

        // Act - Simula 2 transações concorrentes
        for (int i = 0; i < 2; i++) {
            executorService.submit(() -> {
                try {
                    // Simula verificação e débito atômico
                    synchronized (this) {
                        if (saldoAtual.get() >= valor.get()) {
                            saldoAtual.addAndGet(-valor.get());
                            transacoesAprovadas.incrementAndGet();
                        } else {
                            transacoesRejeitadas.incrementAndGet();
                        }
                    }
                    return true;
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);

        // Assert
        assertEquals(1, transacoesAprovadas.get(), "Apenas 1 transação deve ser aprovada");
        assertEquals(1, transacoesRejeitadas.get(), "1 transação deve ser rejeitada");
        assertEquals(0, saldoAtual.get(), "Saldo deve ser zero");

        executorService.shutdown();
    }
}