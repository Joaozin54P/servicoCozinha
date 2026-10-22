package com.example.servicocozinha.controller.adapter;

import java.util.List;

import com.example.servicocozinha.controller.dto.request.ReceberPedidoRequest;
import com.example.servicocozinha.controller.dto.response.PedidoCozinhaResponse;
import com.example.servicocozinha.entity.ItemCozinha;
import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.service.NovoPedidoCozinha;

public class CozinhaControllerAdapter {

    private CozinhaControllerAdapter() {
    }

    public static NovoPedidoCozinha castRequest(ReceberPedidoRequest request) {
        List<NovoPedidoCozinha.Item> itens = request.itens() == null ? null : request.itens().stream()
                .map(i -> new NovoPedidoCozinha.Item(i.nomeProduto(), i.quantidade(), i.adicionais(), i.observacao()))
                .toList();

        return new NovoPedidoCozinha(
                request.pedidoId(),
                request.senhaRetirada(),
                request.clienteId(),
                request.observacao(),
                request.criadoEm(),
                itens);
    }

    public static PedidoCozinhaResponse castResponse(PedidoCozinha pedido) {
        List<PedidoCozinhaResponse.ItemResponse> itens = pedido.itens().stream()
                .map(CozinhaControllerAdapter::castResponse)
                .toList();

        return new PedidoCozinhaResponse(
                pedido.pedidoId(),
                pedido.senhaRetirada(),
                pedido.clienteId(),
                pedido.status().name(),
                pedido.observacao(),
                pedido.criadoEm(),
                pedido.recebidoEm(),
                pedido.atualizadoEm(),
                itens);
    }

    private static PedidoCozinhaResponse.ItemResponse castResponse(ItemCozinha item) {
        return new PedidoCozinhaResponse.ItemResponse(
                item.nomeProduto(), item.quantidade(), item.adicionais(), item.observacao());
    }
}
