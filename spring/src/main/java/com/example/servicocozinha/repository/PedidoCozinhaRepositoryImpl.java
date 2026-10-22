package com.example.servicocozinha.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.exception.PedidoJaRecebidoException;
import com.example.servicocozinha.repository.adapter.CozinhaRepositoryAdapter;
import com.example.servicocozinha.repository.jpa.PedidoCozinhaRepositoryWithJpa;
import com.example.servicocozinha.repository.orm.PedidoCozinhaOrm;

@Repository
public class PedidoCozinhaRepositoryImpl implements PedidoCozinhaRepository {

    private final PedidoCozinhaRepositoryWithJpa repository;

    public PedidoCozinhaRepositoryImpl(PedidoCozinhaRepositoryWithJpa repository) {
        this.repository = repository;
    }

    @Override
    public PedidoCozinha salvar(PedidoCozinha pedido) {
        PedidoCozinhaOrm orm = CozinhaRepositoryAdapter.castEntity(pedido);
        try {
            // saveAndFlush: grava na hora, assim a restricao unica reclama aqui (e nao so no commit)
            return CozinhaRepositoryAdapter.castOrm(repository.saveAndFlush(orm));
        } catch (DataIntegrityViolationException e) {
            throw new PedidoJaRecebidoException("O pedido " + pedido.pedidoId() + " ja esta na cozinha");
        }
    }

    @Override
    public Optional<PedidoCozinha> buscarPorPedidoId(Long pedidoId) {
        return repository.findByPedidoId(pedidoId).map(CozinhaRepositoryAdapter::castOrm);
    }

    @Override
    public Optional<PedidoCozinha> buscarPorPedidoIdComLock(Long pedidoId) {
        return repository.buscarComLock(pedidoId).map(CozinhaRepositoryAdapter::castOrm);
    }

    @Override
    public List<PedidoCozinha> listarPorStatus(Collection<StatusPreparo> status) {
        return repository.findByStatusInOrderByCriadoEmAsc(status).stream()
                .map(CozinhaRepositoryAdapter::castOrm)
                .toList();
    }
}
