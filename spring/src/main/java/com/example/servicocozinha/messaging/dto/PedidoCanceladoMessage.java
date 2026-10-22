package com.example.servicocozinha.messaging.dto;

// JSON da fila "pedido-cancelado" (o servicoPedido PUBLICA, a cozinha le)
public record PedidoCanceladoMessage(
        Long pedidoId
) {
}
