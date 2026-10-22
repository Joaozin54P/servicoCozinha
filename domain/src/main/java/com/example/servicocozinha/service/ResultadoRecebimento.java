package com.example.servicocozinha.service;

import com.example.servicocozinha.entity.PedidoCozinha;

// repetido = true quando o pedido ja tinha chegado antes (nada foi duplicado)
public record ResultadoRecebimento(
        PedidoCozinha pedido,
        boolean repetido
) {
}
