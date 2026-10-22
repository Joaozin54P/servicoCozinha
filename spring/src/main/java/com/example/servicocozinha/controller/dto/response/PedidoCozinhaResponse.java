package com.example.servicocozinha.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PedidoCozinhaResponse(
        Long pedidoId,
        String senhaRetirada,
        Long clienteId,
        String status,
        String observacao,
        LocalDateTime criadoEm,
        LocalDateTime recebidoEm,
        LocalDateTime atualizadoEm,
        List<ItemResponse> itens
) {
    public record ItemResponse(
            String nomeProduto,
            int quantidade,
            List<String> adicionais,
            String observacao
    ) {
    }
}
