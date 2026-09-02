package com.teste.miniautorizador.exception;

import com.teste.miniautorizador.dto.CartaoResponseDTO;
import lombok.Getter;

@Getter
public class CartaoExistenteException extends RuntimeException {

    private final CartaoResponseDTO cartaoResponse;

    public CartaoExistenteException(CartaoResponseDTO cartaoResponse) {
        super("Cartão já existe com o número: " + cartaoResponse.getNumeroCartao());
        this.cartaoResponse = cartaoResponse;
    }

    public CartaoExistenteException(String message, CartaoResponseDTO cartaoResponse) {
        super(message);
        this.cartaoResponse = cartaoResponse;
    }
}