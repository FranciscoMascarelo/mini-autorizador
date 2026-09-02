package com.teste.miniautorizador.service.impl;

import com.teste.miniautorizador.dto.CartaoRequestDTO;
import com.teste.miniautorizador.dto.CartaoResponseDTO;
import com.teste.miniautorizador.dto.TransacaoRequestDTO;
import com.teste.miniautorizador.entity.Cartao;
import com.teste.miniautorizador.exception.CartaoExistenteException;
import com.teste.miniautorizador.exception.CartaoInexistenteException;
import com.teste.miniautorizador.exception.TransacaoNaoAutorizadaException;
import com.teste.miniautorizador.repository.CartaoRepository;
import com.teste.miniautorizador.service.CartaoService;
import com.teste.miniautorizador.validation.RegraAutorizacao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartaoServiceImpl implements CartaoService {

    private final CartaoRepository cartaoRepository;
    private final List<RegraAutorizacao> regrasAutorizacao;
    private final PasswordEncoder passwordEncoder; // Injetar o encoder

    @Override
    public CartaoResponseDTO criarCartao(CartaoRequestDTO request) {
        // Verifica se cartão já existe
        cartaoRepository.findByNumeroCartao(request.getNumeroCartao())
                .ifPresent(cartaoExistente -> {
                    throw new CartaoExistenteException(
                            CartaoResponseDTO.builder()
                                    .numeroCartao(cartaoExistente.getNumeroCartao())
                                    .senha(cartaoExistente.getSenha())
                                    .build()
                    );
                });

        // Criptografa a senha antes de salvar
        String senhaCriptografada = passwordEncoder.encode(request.getSenha());

        // Cria novo cartão com senha criptografada
        Cartao novoCartao = Cartao.builder()
                .numeroCartao(request.getNumeroCartao())
                .senha(senhaCriptografada)
                .saldo(new BigDecimal("500.00"))
                .build();

        Cartao cartaoSalvo = cartaoRepository.save(novoCartao);

        // Retorna o DTO com a senha original (não criptografada)
        return CartaoResponseDTO.builder()
                .numeroCartao(cartaoSalvo.getNumeroCartao())
                .senha(request.getSenha()) // Retorna senha original
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal consultarSaldo(String numeroCartao) {
        return cartaoRepository.findByNumeroCartao(numeroCartao)
                .map(Cartao::getSaldo)
                .orElseThrow(() -> new CartaoInexistenteException(numeroCartao));
    }

    @Override
    public void autorizarTransacao(TransacaoRequestDTO transacao) {
        // Busca cartão com lock pessimista
        Cartao cartao = cartaoRepository.findByNumeroCartaoWithLock(transacao.getNumeroCartao())
                .orElseThrow(() -> new TransacaoNaoAutorizadaException("CARTAO_INEXISTENTE"));

        // Executa regras de validação
        regrasAutorizacao.stream()
                .sorted(Comparator.comparingInt(RegraAutorizacao::getOrdem))
                .forEach(regra -> regra.validar(cartao, transacao));

        // Atualiza saldo
        cartao.setSaldo(cartao.getSaldo().subtract(transacao.getValor()));
        cartaoRepository.save(cartao);
    }

}
