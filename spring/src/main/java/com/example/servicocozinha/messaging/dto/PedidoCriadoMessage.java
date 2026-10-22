package com.example.servicocozinha.messaging.dto;

import java.time.LocalDateTime;
import java.util.List;

// JSON da fila "pedido-criado" (o servicoPedido PUBLICA, a cozinha le).
// Mesmo formato que o servicoPedido envia hoje (clienteId, nao e-mail).
public record PedidoCriadoMessage(
        Long pedidoId,
        String senhaRetirada,
        Long clienteId,
        String observacao,
        LocalDateTime criadoEm,
        List<ItemMessage> itens
) {
    public record ItemMessage(
            String nomeProduto,
            int quantidade,
            List<String> adicionais,
            String observacao
    ) {
    }
}
