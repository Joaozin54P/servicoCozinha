package com.example.servicocozinha.entity;

// Onde o pedido esta DENTRO da cozinha.
//   NA_FILA -> EM_PREPARO -> PRONTO
// CANCELADO existe porque o servicoPedido pode avisar "o cliente cancelou". Ele e aceito tambem
// em EM_PREPARO: o aviso de cancelamento e o "comecei" da cozinha viajam em filas diferentes e podem se
// cruzar; quem manda e o servicoPedido (ele so cancela se o pedido ainda estava CRIADO).
public enum StatusPreparo {
    NA_FILA,
    EM_PREPARO,
    PRONTO,
    CANCELADO;

    public boolean podeIrPara(StatusPreparo destino) {
        return switch (this) {
            case NA_FILA -> destino == EM_PREPARO || destino == CANCELADO;
            case EM_PREPARO -> destino == PRONTO || destino == CANCELADO;
            case PRONTO, CANCELADO -> false;
        };
    }

    // ainda tem trabalho a fazer? (o painel da cozinha mostra so estes)
    public boolean ativo() {
        return this == NA_FILA || this == EM_PREPARO;
    }
}
