package com.teste.miniautorizador.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(
        description = "DTO de resposta com dados do cartão",
        name = "CartaoResponse"
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartaoResponseDTO {
    private String numeroCartao;
    private String senha;
}
