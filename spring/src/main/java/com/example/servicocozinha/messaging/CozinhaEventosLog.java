package com.example.servicocozinha.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.servicocozinha.event.StatusPreparoAlteradoEvent;

// Usado quando app.mensageria.habilitada=false (sem RabbitMQ): so registra no log.
// Atencao: nesse modo o servicoPedido NAO e avisado, entao o status la nao acompanha o da cozinha.
@Component
@ConditionalOnProperty(prefix = "app.mensageria", name = "habilitada", havingValue = "false", matchIfMissing = true)
public class CozinhaEventosLog {

    private static final Logger log = LoggerFactory.getLogger(CozinhaEventosLog.class);

    @EventListener
    public void aoMudarDeStatus(StatusPreparoAlteradoEvent evento) {
        log.info("Pedido #{}: {} -> {} (mensageria desligada: o servicoPedido nao foi avisado)",
                evento.pedidoId(), evento.statusAnterior(), evento.novoStatus());
    }
}
