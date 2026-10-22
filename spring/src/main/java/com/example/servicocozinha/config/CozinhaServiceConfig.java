package com.example.servicocozinha.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.servicocozinha.messaging.CozinhaEventPublisher;
import com.example.servicocozinha.repository.PedidoCozinhaRepository;
import com.example.servicocozinha.service.AtualizarPreparoService;
import com.example.servicocozinha.service.ConsultarFilaService;
import com.example.servicocozinha.service.ReceberPedidoService;

// As classes de servico sao Java puro (sem @Service). Aqui elas viram beans.
// Os metodos marcados com @Transactional nelas passam a abrir transacao porque o Spring
// cria um "proxy" em volta de cada bean.
@Configuration
public class CozinhaServiceConfig {

    @Bean
    public ReceberPedidoService receberPedidoService(PedidoCozinhaRepository repository) {
        return new ReceberPedidoService(repository);
    }

    @Bean
    public AtualizarPreparoService atualizarPreparoService(PedidoCozinhaRepository repository,
                                                           CozinhaEventPublisher eventPublisher) {
        return new AtualizarPreparoService(repository, eventPublisher);
    }

    @Bean
    public ConsultarFilaService consultarFilaService(PedidoCozinhaRepository repository) {
        return new ConsultarFilaService(repository);
    }
}
