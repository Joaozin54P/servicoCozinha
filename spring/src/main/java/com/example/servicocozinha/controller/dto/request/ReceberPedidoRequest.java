package com.example.servicocozinha.controller.dto.request;

import java.time.LocalDateTime;
import java.util.List;

// {"pedidoId":1,"senhaRetirada":"001","clienteId":5,"observacao":null,"criadoEm":"2026-10-01T12:00:00",
//  "itens":[{"nomeProduto":"Burger Bacon","quantidade":2,"adicionais":["Bacon extra"],"observacao":"sem cebola"}]}
// (o mesmo JSON da mensagem "pedido-criado" que o servicoPedido publica)
public record ReceberPedidoRequest(
        Long pedidoId,
        String senhaRetirada,
        Long clienteId,
        String observacao,
        LocalDateTime criadoEm,
        List<ItemRequest> itens
) {
    public record ItemRequest(
            String nomeProduto,
            Integer quantidade,
            List<String> adicionais,
            String observacao
    ) {
    }
}
