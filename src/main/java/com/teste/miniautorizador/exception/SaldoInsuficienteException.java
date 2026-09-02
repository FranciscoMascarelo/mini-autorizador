package com.teste.miniautorizador.exception;

import java.math.BigDecimal;

public class SaldoInsuficienteException extends TransacaoNaoAutorizadaException {

    private final BigDecimal saldoAtual;
    private final BigDecimal valorTransacao;

    public SaldoInsuficienteException(BigDecimal saldoAtual, BigDecimal valorTransacao) {
        super(MotivoRecusa.SALDO_INSUFICIENTE);
        this.saldoAtual = saldoAtual;
        this.valorTransacao = valorTransacao;
    }

    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }

    public BigDecimal getValorTransacao() {
        return valorTransacao;
    }

    public BigDecimal getValorFaltante() {
        return valorTransacao.subtract(saldoAtual);
    }
}
