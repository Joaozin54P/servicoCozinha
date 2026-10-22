package com.example.servicocozinha.service;

import java.util.List;

import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.exception.NaoEncontradoException;
import com.example.servicocozinha.repository.PedidoCozinhaRepository;

// So consulta (nao altera nada). Sempre do pedido mais antigo para o mais novo: quem chegou primeiro, sai primeiro.
public class ConsultarFilaService {

    private final PedidoCozinhaRepository repository;

    public ConsultarFilaService(PedidoCozinhaRepository repository) {
        this.repository = repository;
    }

    // o painel da cozinha: so o que ainda tem trabalho (NA_FILA e EM_PREPARO)
    public List<PedidoCozinha> filaAtiva() {
        return repository.listarPorStatus(List.of(StatusPreparo.NA_FILA, StatusPreparo.EM_PREPARO));
    }

    public List<PedidoCozinha> listarPorStatus(StatusPreparo status) {
        return repository.listarPorStatus(List.of(status));
    }

    public PedidoCozinha buscar(Long pedidoId) {
        return repository.buscarPorPedidoId(pedidoId)
                .orElseThrow(() -> new NaoEncontradoException("Pedido " + pedidoId + " nao esta na cozinha"));
    }
}
