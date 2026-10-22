package com.example.servicocozinha.exception;

// o banco recusou porque ja existe um pedido da cozinha com este pedidoId
// (a mesma mensagem chegou duas vezes, ou duas ao mesmo tempo)
public class PedidoJaRecebidoException extends RuntimeException {
    public PedidoJaRecebidoException(String mensagem) {
        super(mensagem);
    }
}
