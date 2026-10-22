package com.example.servicocozinha.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.example.servicocozinha.config.RabbitMQConfig;
import com.example.servicocozinha.exception.RegraDeNegocioException;
import com.example.servicocozinha.messaging.adapter.CozinhaMessagingAdapter;
import com.example.servicocozinha.messaging.dto.PedidoCriadoMessage;
import com.example.servicocozinha.service.ReceberPedidoService;
import com.example.servicocozinha.service.ResultadoRecebimento;

// Pedido aprovado chegando do servicoPedido: entra na fila da cozinha
@ConditionalOnProperty(prefix = "app.mensageria", name = "habilitada", havingValue = "true")
@Component
public class PedidoCriadoConsumer {

    private static final Logger log = LoggerFactory.getLogger(PedidoCriadoConsumer.class);

    private final ReceberPedidoService receberPedido;

    public PedidoCriadoConsumer(ReceberPedidoService receberPedido) {
        this.receberPedido = receberPedido;
    }

    @RabbitListener(queues = RabbitMQConfig.FILA_PEDIDO_CRIADO)
    public void receber(PedidoCriadoMessage mensagem) {
        try {
            ResultadoRecebimento resultado = receberPedido.receber(CozinhaMessagingAdapter.castMessage(mensagem));
            log.info("PEDIDO -> pedido #{} {}", mensagem.pedidoId(),
                    resultado.repetido() ? "ja estava na cozinha (mensagem repetida ignorada)" : "entrou na fila");
        } catch (RegraDeNegocioException e) {
            // mensagem invalida: registra e descarta (reenviar a mesma mensagem nao resolveria).
            // Qualquer OUTRO erro (ex: banco fora do ar) sobe de proposito: a mensagem volta para a fila
            // e e tentada de novo, senao o pedido do cliente se perderia.
            log.warn("Pedido ignorado pela cozinha ({}): {}", mensagem, e.getMessage());
        }
    }
}
