package com.teste.miniautorizador.validation;

import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;

public interface RegraAutorizacao {
    int getOrdem();
    void validar(Cartao cartao, TransacaoRequestDTO transacao);
}
