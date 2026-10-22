package com.example.servicocozinha.entity;

import java.util.List;

// o que preparar: so nome, quantidade e detalhes (a cozinha nao precisa saber preco)
public record ItemCozinha(
        Long id,
        String nomeProduto,
        int quantidade,
        List<String> adicionais,
        String observacao
) {
}
