package com.example.servicocozinha.entity;

import java.time.LocalDateTime;
import java.util.List;

// Um pedido aprovado, do ponto de vista da cozinha.
// "pedidoId" e o ID do pedido no servicoPedido (e por ele que os dois servicos conversam);
// "id" e so a chave do banco da cozinha.
public record PedidoCozinha(
        Long id,
        Long pedidoId,
        String senhaRetirada,
        Long clienteId,
        String observacao,
        StatusPreparo status,
        List<ItemCozinha> itens,
        LocalDateTime criadoEm,      // quando o cliente fez o pedido
        LocalDateTime recebidoEm,    // quando chegou na cozinha
        LocalDateTime atualizadoEm
) {

    public PedidoCozinha comStatus(StatusPreparo novoStatus) {
        return new PedidoCozinha(id, pedidoId, senhaRetirada, clienteId, observacao, novoStatus,
                itens, criadoEm, recebidoEm, LocalDateTime.now());
    }
}
