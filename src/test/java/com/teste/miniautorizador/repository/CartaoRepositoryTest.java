package com.teste.miniautorizador.repository;

import com.teste.miniautorizador.entity.Cartao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CartaoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CartaoRepository cartaoRepository;

    private Cartao cartao;

    @BeforeEach
    void setUp() {
        cartao = Cartao.builder()
                .numeroCartao("6549873025634501")
                .senha("$2a$10$hashedPassword")
                .saldo(new BigDecimal("500.00"))
                .build();

        entityManager.persistAndFlush(cartao);
    }

    @Test
    @DisplayName("Deve encontrar cartão por número")
    void deveEncontrarCartaoPorNumero() {
        // Act
        Optional<Cartao> cartaoEncontrado = cartaoRepository
                .findByNumeroCartao("6549873025634501");

        // Assert
        assertTrue(cartaoEncontrado.isPresent());
        assertEquals("6549873025634501", cartaoEncontrado.get().getNumeroCartao());
        assertEquals(new BigDecimal("500.00"), cartaoEncontrado.get().getSaldo());
    }

    @Test
    @DisplayName("Deve retornar vazio para cartão inexistente")
    void deveRetornarVazioParaCartaoInexistente() {
        // Act
        Optional<Cartao> cartaoEncontrado = cartaoRepository
                .findByNumeroCartao("9999999999999999");

        // Assert
        assertFalse(cartaoEncontrado.isPresent());
    }

    @Test
    @DisplayName("Deve encontrar cartão com lock pessimista")
    void deveEncontrarCartaoComLock() {
        // Act
        Optional<Cartao> cartaoEncontrado = cartaoRepository
                .findByNumeroCartaoWithLock("6549873025634501");

        // Assert
        assertTrue(cartaoEncontrado.isPresent());
        assertEquals("6549873025634501", cartaoEncontrado.get().getNumeroCartao());
    }

    @Test
    @DisplayName("Deve salvar cartão com dados corretos")
    void deveSalvarCartao() {
        // Arrange
        Cartao novoCartao = Cartao.builder()
                .numeroCartao("1111222233334444")
                .senha("$2a$10$anotherHash")
                .saldo(new BigDecimal("500.00"))
                .build();

        // Act
        Cartao cartaoSalvo = cartaoRepository.save(novoCartao);

        // Assert
        assertNotNull(cartaoSalvo.getId());
        assertEquals("1111222233334444", cartaoSalvo.getNumeroCartao());
        assertEquals(new BigDecimal("500.00"), cartaoSalvo.getSaldo());
    }

    @Test
    @DisplayName("Deve atualizar saldo do cartão")
    void deveAtualizarSaldo() {
        // Arrange
        Cartao cartaoParaAtualizar = cartaoRepository
                .findByNumeroCartao("6549873025634501")
                .orElseThrow();

        // Act
        cartaoParaAtualizar.setSaldo(new BigDecimal("400.00"));
        Cartao cartaoAtualizado = cartaoRepository.save(cartaoParaAtualizar);

        // Assert
        assertEquals(new BigDecimal("400.00"), cartaoAtualizado.getSaldo());
    }

    @Test
    @DisplayName("Deve excluir cartão")
    void deveExcluirCartao() {
        // Arrange
        Cartao cartaoParaExcluir = cartaoRepository
                .findByNumeroCartao("6549873025634501")
                .orElseThrow();

        // Act
        cartaoRepository.delete(cartaoParaExcluir);
        cartaoRepository.flush();

        // Assert
        Optional<Cartao> cartaoExcluido = cartaoRepository
                .findByNumeroCartao("6549873025634501");
        assertFalse(cartaoExcluido.isPresent());
    }
}