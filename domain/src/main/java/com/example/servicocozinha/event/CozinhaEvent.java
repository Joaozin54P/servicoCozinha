package com.example.servicocozinha.event;

// marca tudo que "aconteceu" na cozinha. O dominio so diz o que aconteceu;
// quem escuta (RabbitMQ, log...) e decidido na camada spring, com eventos do Spring.
public interface CozinhaEvent {
}
