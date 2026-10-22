package com.example.servicocozinha.messaging.dto;

// JSON da fila "pedido-status-cozinha" (a cozinha PUBLICA, o servicoPedido le)
// status: "EM_PREPARO" ou "PRONTO"
public record StatusCozinhaMessage(
        Long pedidoId,
        String status
) {
}
