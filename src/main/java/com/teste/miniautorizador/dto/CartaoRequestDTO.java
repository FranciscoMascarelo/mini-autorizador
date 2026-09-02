package com.teste.miniautorizador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
        description = "DTO para criação de cartão",
        name = "CartaoRequest"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartaoRequestDTO {
    @NotBlank(message = "Número do cartão é obrigatório")
    private String numeroCartao;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;
}