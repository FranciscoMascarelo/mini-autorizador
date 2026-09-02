package com.teste.miniautorizador.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cartoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 16)
    private String numeroCartao;

    @Column(nullable = false, length = 60) // BCrypt gera hash de 60 caracteres
    private String senha;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal saldo;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (saldo == null) {
            saldo = new BigDecimal("500.00");
        }
    }
}