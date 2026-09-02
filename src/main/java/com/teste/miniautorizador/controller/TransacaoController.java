package com.teste.miniautorizador.controller;

import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.service.CartaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final CartaoService cartaoService;

    @Operation(
            summary = "Realizar transação",
            description = """
            Autoriza e processa uma transação de débito no cartão.
            
            Regras de autorização:
            - Cartão deve existir
            - Senha deve ser válida
            - Saldo deve ser suficiente
            """,
            method = "POST"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transação autorizada com sucesso",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = @ExampleObject(value = "OK")
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Transação não autorizada",
                    content = @Content(
                            mediaType = MediaType.TEXT_PLAIN_VALUE,
                            examples = {
                                    @ExampleObject(
                                            name = "Cartão inexistente",
                                            value = "CARTAO_INEXISTENTE"
                                    ),
                                    @ExampleObject(
                                            name = "Senha inválida",
                                            value = "SENHA_INVALIDA"
                                    ),
                                    @ExampleObject(
                                            name = "Saldo insuficiente",
                                            value = "SALDO_INSUFICIENTE"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<String> realizarTransacao(@Valid @RequestBody TransacaoRequestDTO request) {
        cartaoService.autorizarTransacao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("OK");
    }
}
