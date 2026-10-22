package com.example.servicocozinha.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// nomes das filas = contrato com o servicoPedido (ver README do servicoPedido).
// Todas durable, igual la: declarar de novo com as mesmas propriedades e permitido.
@ConditionalOnProperty(prefix = "app.mensageria", name = "habilitada", havingValue = "true")
@Configuration
public class RabbitMQConfig {

    // servicoPedido -> servicoCozinha
    public static final String FILA_PEDIDO_CRIADO = "pedido-criado";
    public static final String FILA_PEDIDO_CANCELADO = "pedido-cancelado";

    // servicoCozinha -> servicoPedido
    public static final String FILA_STATUS_COZINHA = "pedido-status-cozinha";

    @Bean
    public Queue filaPedidoCriado() {
        return new Queue(FILA_PEDIDO_CRIADO, true);
    }

    @Bean
    public Queue filaPedidoCancelado() {
        return new Queue(FILA_PEDIDO_CANCELADO, true);
    }

    @Bean
    public Queue filaStatusCozinha() {
        return new Queue(FILA_STATUS_COZINHA, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
