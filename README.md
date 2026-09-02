# 🏦 Mini Autorizador

Sistema de autorização de transações para cartões de benefícios (Vale Refeição, Vale Alimentação, etc.) desenvolvido com Spring Boot.

## 📋 Sobre o Projeto

O Mini Autorizador é uma aplicação REST que simula o processo de autorização de transações realizadas com cartões de benefícios. O sistema processa requisições de débito, aplicando regras de negócio para aprovar ou recusar transações com base em critérios como existência do cartão, validade da senha e saldo disponível.

### Funcionalidades Principais

- Criação de cartões com saldo inicial de R$ 500,00
- Consulta de saldo em tempo real
- Autorização de transações com validações de segurança
- Criptografia de senhas com BCrypt
- Controle de concorrência para transações simultâneas
- Documentação interativa da API com Swagger

## 🛠️ Tecnologias Utilizadas

### Stack Principal

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java | 17 | Linguagem de programação |
| Spring Boot | 3.2.0 | Framework principal |
| Spring Data JPA | 3.2.0 | Persistência de dados |
| Spring Security Crypto | 6.2.0 | Criptografia de senhas |
| MySQL | 5.7 | Banco de dados relacional |
| H2 Database | 2.x | Banco em memória para testes |
| Maven | 3.9+ | Gerenciamento de dependências |
| Docker | 24+ | Containerização |
| JUnit 5 | 5.10+ | Testes automatizados |
| Mockito | 5.7+ | Mock para testes unitários |
| Lombok | 1.18+ | Redução de boilerplate |
| SpringDoc OpenAPI | 2.3.0 | Documentação da API (Swagger) |

### Dependências Principais

```xml
<dependencies>
    <!-- Web/REST -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Persistência -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Validação -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Segurança (Criptografia) -->
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-crypto</artifactId>
    </dependency>
    
    <!-- Banco de Dados -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Testes -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Documentação -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

## PARTE 3 - Arquitetura

## 🏗️ Arquitetura

### Visão Geral

O projeto segue uma arquitetura em camadas (Layered Architecture) com princípios de Clean Architecture e Domain-Driven Design (DDD) simplificado.

### Estrutura de Pacotes


### Fluxo da Aplicação

1. **Controller** recebe a requisição HTTP
2. **DTO** valida os dados de entrada
3. **Service** orquestra a lógica de negócio
4. **Validation** aplica as regras de autorização
5. **Repository** persiste e consulta dados
6. **Entity** representa o modelo de domínio
7. **Exception Handler** trata erros e retorna respostas apropriadas

## 🎯 Design Patterns Utilizados

### 1. Strategy Pattern (Padrão de Estratégia)

Utilizado para implementar as regras de autorização de forma flexível e extensível.

```java
// Interface Strategy
public interface RegraAutorizacao {
    int getOrdem();
    void validar(Cartao cartao, TransacaoRequestDTO transacao);
}

// Implementações concretas
@Component
@Order(1)
public class RegraSenhaValida implements RegraAutorizacao {
    // Implementação específica
}

@Component
@Order(2)
public class RegraSaldoSuficiente implements RegraAutorizacao {
    // Implementação específica
}
````
### Benefícios:

- Fácil adicionar novas regras sem modificar código existente

- Cada regra é isolada e testável independentemente

- Ordenação controlada das validações

### 2. Repository Pattern (Padrão de Repositório)
```java
@Repository
public interface CartaoRepository extends JpaRepository<Cartao, Long> {
Optional<Cartao> findByNumeroCartao(String numeroCartao);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cartao c WHERE c.numeroCartao = :numeroCartao")
    Optional<Cartao> findByNumeroCartaoWithLock(@Param("numeroCartao") String numeroCartao);
}
````

### 3. DTO Pattern (Data Transfer Object)
Separação entre o modelo de domínio e o modelo de transferência:
```java
// Entidade de domínio
@Entity
public class Cartao {
private String senha; // Senha criptografada
// ...
}

// DTO de transferência
public class CartaoRequestDTO {
@NotBlank
private String numeroCartao;
@NotBlank
private String senha; // Senha em texto puro (para validação)
}
```

### 4. Exception Handling Pattern
Tratamento centralizado de exceções com @RestControllerAdvice:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CartaoExistenteException.class)
    public ResponseEntity<CartaoResponseDTO> handleCartaoExistente(CartaoExistenteException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ex.getCartaoResponse());
    }
}
```
### 5. Builder Pattern
Utilizado através do Lombok para construção de objetos complexos:

```java
Cartao cartao = Cartao.builder()
    .numeroCartao("6549873025634501")
    .senha(senhaCriptografada)
    .saldo(new BigDecimal("500.00"))
    .build();
```


## PARTE 5 - Segurança

## 🔒 Segurança

### Criptografia de Senhas

As senhas são criptografadas usando BCrypt (força 10), que é o algoritmo recomendado pelo OWASP para hash de senhas.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

### Características do BCrypt:

- Salt automático (16 bytes aleatórios)

- Função adaptativa (pode aumentar a força conforme hardware)

- Resistente a ataques de força bruta

- Gera hash de 60 caracteres

### Controle de Concorrência
#### Locking Otimista
```java
@Entity
public class Cartao {
    @Version
    private Long version;
}
```

#### Locking Pessimista
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Cartao c WHERE c.numeroCartao = :numeroCartao")
Optional<Cartao> findByNumeroCartaoWithLock(@Param("numeroCartao") String numeroCartao);
```
