package com.example.servicocozinha.messaging;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.example.servicocozinha.event.CozinhaEvent;

// Liga o dominio ao Spring: o dominio chama publicar(evento) e aqui o evento vira um evento do Spring.
// A partir dai, QUEM QUISER reagir so cria um @EventListener (ver CozinhaEventosRabbit e CozinhaEventosLog).
@Component
public class CozinhaEventPublisherSpring implements CozinhaEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CozinhaEventPublisherSpring(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(CozinhaEvent evento) {
        applicationEventPublisher.publishEvent(evento);
    }
}
