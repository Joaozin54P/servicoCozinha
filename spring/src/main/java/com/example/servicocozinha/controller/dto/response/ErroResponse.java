package com.example.servicocozinha.controller.dto.response;

// formato unico de erro da API: {"erro": "mensagem"}  (facil de mostrar no front)
public record ErroResponse(
        String erro
) {
}
