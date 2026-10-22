package com.example.servicocozinha.service;

import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.event.StatusPreparoAlteradoEvent;
import com.example.servicocozinha.exception.NaoEncontradoException;
import com.example.servicocozinha.exception.RegraDeNegocioException;
import com.example.servicocozinha.messaging.CozinhaEventPublisher;
import com.example.servicocozinha.repository.PedidoCozinhaRepository;

import jakarta.transaction.Transactional;

// So muda o status do pedido dentro da cozinha e avisa quem estiver escutando
// (la na camada spring, o aviso EM_PREPARO / PRONTO vai para o servicoPedido).
// Cada metodo trava a linha do pedido: duas mudancas ao mesmo tempo nunca passam as duas.
public class AtualizarPreparoService {

    private final PedidoCozinhaRepository repository;
    private final CozinhaEventPublisher eventPublisher;

    public AtualizarPreparoService(PedidoCozinhaRepository repository, CozinhaEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    // "comecei a preparar"
    @Transactional
    public PedidoCozinha iniciar(Long pedidoId) {
        return mudar(pedidoId, StatusPreparo.EM_PREPARO);
    }

    // "ficou pronto"
    @Transactional
    public PedidoCozinha marcarPronto(Long pedidoId) {
        return mudar(pedidoId, StatusPreparo.PRONTO);
    }

    // o servicoPedido avisou que o cliente cancelou: tira da fila
    @Transactional
    public PedidoCozinha cancelar(Long pedidoId) {
        return mudar(pedidoId, StatusPreparo.CANCELADO);
    }

    private PedidoCozinha mudar(Long pedidoId, StatusPreparo destino) {
        PedidoCozinha pedido = repository.buscarPorPedidoIdComLock(pedidoId)
                .orElseThrow(() -> new NaoEncontradoException("Pedido " + pedidoId + " nao esta na cozinha"));

        if (!pedido.status().podeIrPara(destino)) {
            throw new RegraDeNegocioException(
                    "O pedido " + pedidoId + " esta " + pedido.status() + " e nao pode ir para " + destino);
        }

        PedidoCozinha atualizado = repository.salvar(pedido.comStatus(destino));
        eventPublisher.publicar(new StatusPreparoAlteradoEvent(pedidoId, pedido.status(), destino));
        return atualizado;
    }
}
