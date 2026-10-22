package com.example.servicocozinha.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.example.servicocozinha.config.RabbitMQConfig;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.event.StatusPreparoAlteradoEvent;
import com.example.servicocozinha.messaging.dto.StatusCozinhaMessage;

// Avisa o servicoPedido (fila pedido-status-cozinha) quando a cozinha comeca ou termina um pedido.
// O @EventListener roda na mesma thread e na mesma transacao de quem publicou: se o RabbitMQ falhar,
// o erro volta e a mudanca de status sofre rollback (a cozinha nunca fica "pronta" sem o Pedido saber).
// CANCELADO nao e avisado: foi o proprio servicoPedido que mandou cancelar.
@ConditionalOnProperty(prefix = "app.mensageria", name = "habilitada", havingValue = "true")
@Component
public class CozinhaEventosRabbit {

    private static final Logger log = LoggerFactory.getLogger(CozinhaEventosRabbit.class);

    private final RabbitTemplate rabbitTemplate;

    public CozinhaEventosRabbit(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @EventListener
    public void aoMudarDeStatus(StatusPreparoAlteradoEvent evento) {
        if (evento.novoStatus() != StatusPreparo.EM_PREPARO && evento.novoStatus() != StatusPreparo.PRONTO) {
            return;
        }

        rabbitTemplate.convertAndSend(RabbitMQConfig.FILA_STATUS_COZINHA,
                new StatusCozinhaMessage(evento.pedidoId(), evento.novoStatus().name()));
        log.info("Pedido #{}: {} -> {} (avisado ao servicoPedido pelo RabbitMQ)",
                evento.pedidoId(), evento.statusAnterior(), evento.novoStatus());
    }
}
