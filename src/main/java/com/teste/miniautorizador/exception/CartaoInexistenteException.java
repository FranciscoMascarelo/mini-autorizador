package com.teste.miniautorizador.exception;

public class CartaoInexistenteException extends RuntimeException {

    public CartaoInexistenteException(String message, Throwable cause) {
        super(message, cause);
    }

    public CartaoInexistenteException(String numeroCartao) {
        super("Cartão não encontrado: " + numeroCartao);
    }
}
