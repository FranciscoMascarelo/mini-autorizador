package com.teste.miniautorizador.controller;

import com.teste.miniautorizador.dto.CartaoRequestDTO;
import com.teste.miniautorizador.dto.CartaoResponseDTO;
import com.teste.miniautorizador.service.CartaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/cartoes")
@RequiredArgsConstructor
public class CartaoController {

    private final CartaoService cartaoService;

    @Operation(
            summary = "Criar novo cartão",
            description = "Cria um novo cartão com saldo inicial de R$ 500,00",
            method = "POST"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cartão criado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartaoResponseDTO.class),
                            examples = @ExampleObject(
                                    value = """
                    {
                        "numeroCartao": "6549873025634501",
                        "senha": "1234"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Cartão já existe",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CartaoResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<CartaoResponseDTO> criarCartao(@Valid @RequestBody CartaoRequestDTO request) {
        CartaoResponseDTO response = cartaoService.criarCartao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Consultar saldo do cartão",
            description = "Retorna o saldo atual do cartão especificado",
            method = "GET"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Saldo consultado com sucesso",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = BigDecimal.class),
                            examples = @ExampleObject(value = "495.15")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cartão não encontrado",
                    content = @Content
            )
    })
    @GetMapping("/{numeroCartao}")
    public ResponseEntity<BigDecimal> consultarSaldo(@PathVariable String numeroCartao) {
        BigDecimal saldo = cartaoService.consultarSaldo(numeroCartao);
        return ResponseEntity.ok(saldo);
    }
}
