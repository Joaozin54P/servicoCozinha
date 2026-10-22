package com.example.servicocozinha.messaging.adapter;

import com.example.servicocozinha.messaging.dto.PedidoCriadoMessage;
import com.example.servicocozinha.service.NovoPedidoCozinha;

public class CozinhaMessagingAdapter {

    private CozinhaMessagingAdapter() {
    }

    public static NovoPedidoCozinha castMessage(PedidoCriadoMessage mensagem) {
        return new NovoPedidoCozinha(
                mensagem.pedidoId(),
                mensagem.senhaRetirada(),
                mensagem.clienteId(),
                mensagem.observacao(),
                mensagem.criadoEm(),
                mensagem.itens() == null ? null : mensagem.itens().stream()
                        .map(i -> new NovoPedidoCozinha.Item(
                                i.nomeProduto(), i.quantidade(), i.adicionais(), i.observacao()))
                        .toList());
    }
}
