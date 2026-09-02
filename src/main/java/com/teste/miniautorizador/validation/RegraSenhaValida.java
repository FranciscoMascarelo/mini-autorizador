package com.teste.miniautorizador.validation;

import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RegraSenhaValida implements RegraAutorizacao {

    private final PasswordEncoder passwordEncoder;

    public RegraSenhaValida(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public int getOrdem() {
        return 1;
    }

    @Override
    public void validar(Cartao cartao, TransacaoRequestDTO transacao) {
        boolean senhaValida = passwordEncoder.matches(
                transacao.getSenhaCartao(),
                cartao.getSenha()
        );

        if (!senhaValida) {
            throw new TransacaoNaoAutorizadaException("SENHA_INVALIDA");
        }
    }
}