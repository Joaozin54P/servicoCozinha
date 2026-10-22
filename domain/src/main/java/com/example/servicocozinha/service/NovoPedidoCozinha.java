package com.example.servicocozinha.service;

import java.time.LocalDateTime;
import java.util.List;

// o que a cozinha recebe quando um pedido e aprovado (mesmo formato da mensagem "pedido-criado")
public record NovoPedidoCozinha(
        Long pedidoId,
        String senhaRetirada,
        Long clienteId,
        String observacao,
        LocalDateTime criadoEm,
        List<Item> itens
) {
    public record Item(
            String nomeProduto,
            Integer quantidade,
            List<String> adicionais,
            String observacao
    ) {
    }
}
