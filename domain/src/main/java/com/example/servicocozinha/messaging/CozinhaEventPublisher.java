package com.example.servicocozinha.messaging;

import com.example.servicocozinha.event.CozinhaEvent;

// porta de saida: o dominio avisa "aconteceu X" e pronto.
// Na camada spring isso vira um evento do Spring (ApplicationEventPublisher).
public interface CozinhaEventPublisher {
    void publicar(CozinhaEvent evento);
}
