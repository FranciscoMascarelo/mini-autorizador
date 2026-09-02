package com.teste.miniautorizador.validation;

import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class RegraSaldoSuficiente implements RegraAutorizacao {
    @Override
    public int getOrdem() {
        return 2;
    }

    @Override
    public void validar(Cartao cartao, TransacaoRequestDTO transacao) {
        if (cartao.getSaldo().compareTo(transacao.getValor()) < 0) {
            throw new TransacaoNaoAutorizadaException("SALDO_INSUFICIENTE");
        }
    }
}