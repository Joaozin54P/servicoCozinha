package com.example.servicocozinha.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;

public interface PedidoCozinhaRepository {

    // lanca PedidoJaRecebidoException se outro pedido da cozinha ja tem este pedidoId
    PedidoCozinha salvar(PedidoCozinha pedido);

    Optional<PedidoCozinha> buscarPorPedidoId(Long pedidoId);

    // trava a linha ate o fim da transacao: evita duas mudancas de status ao mesmo tempo
    Optional<PedidoCozinha> buscarPorPedidoIdComLock(Long pedidoId);

    // fila da cozinha: mais antigos primeiro
    List<PedidoCozinha> listarPorStatus(Collection<StatusPreparo> status);
}
