package com.example.servicocozinha.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.servicocozinha.config.RabbitMQConfig;
import com.example.servicocozinha.exception.NaoEncontradoException;
import com.example.servicocozinha.exception.RegraDeNegocioException;
import com.example.servicocozinha.messaging.dto.PedidoCanceladoMessage;
import com.example.servicocozinha.service.AtualizarPreparoService;

// O cliente cancelou: o pedido sai da fila da cozinha
@ConditionalOnProperty(prefix = "app.mensageria", name = "habilitada", havingValue = "true")
@Component
public class PedidoCanceladoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCanceladoConsumer.class);

    private final AtualizarPreparoService atualizarPreparo;

    public PedidoCanceladoConsumer(AtualizarPreparoService atualizarPreparo) {
        this.atualizarPreparo = atualizarPreparo;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_PEDIDO_CANCELADO)
    public void receber(PedidoCanceladoMessage mensagem) {
        try {
            atualizarPreparo.cancelar(mensagem.pedidoId());
            log.info("PEDIDO -> pedido #{} cancelado: saiu da fila da cozinha", mensagem.pedidoId());
        } catch (NaoEncontradoException | RegraDeNegocioException e) {
            // pedido que a cozinha nao conhece, ou que ja esta pronto: registra e descarta
            log.warn("Cancelamento ignorado pela cozinha ({}): {}", mensagem, e.getMessage());
        }
    }
}
