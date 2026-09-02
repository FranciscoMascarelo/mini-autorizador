package com.teste.miniautorizador.exception;

import lombok.Getter;

@Getter
public class TransacaoNaoAutorizadaException extends RuntimeException {

    public enum MotivoRecusa {
        CARTAO_INEXISTENTE("CARTAO_INEXISTENTE"),
        SENHA_INVALIDA("SENHA_INVALIDA"),
        SALDO_INSUFICIENTE("SALDO_INSUFICIENTE");

        private final String descricao;

        MotivoRecusa(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private final MotivoRecusa motivo;

    public TransacaoNaoAutorizadaException(String message) {
        super(message);
        this.motivo = MotivoRecusa.valueOf(message);
    }

    public TransacaoNaoAutorizadaException(MotivoRecusa motivo) {
        super(motivo.getDescricao());
        this.motivo = motivo;
    }

    public TransacaoNaoAutorizadaException(String message, Throwable cause) {
        super(message, cause);
        this.motivo = MotivoRecusa.valueOf(message);
    }
}
