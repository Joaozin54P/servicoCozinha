package com.example.servicocozinha.event;

import com.example.servicocozinha.entity.StatusPreparo;

// disparado em TODA mudanca de status (NA_FILA -> EM_PREPARO -> PRONTO, ou CANCELADO)
public record StatusPreparoAlteradoEvent(
        Long pedidoId,
        StatusPreparo statusAnterior,
        StatusPreparo novoStatus
) implements CozinhaEvent {
}
