package com.example.servicocozinha.repository.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.repository.orm.PedidoCozinhaOrm;

import jakarta.persistence.LockModeType;

public interface PedidoCozinhaRepositoryWithJpa extends JpaRepository<PedidoCozinhaOrm, Long> {

    Optional<PedidoCozinhaOrm> findByPedidoId(Long pedidoId);

    // trava a linha do pedido (SELECT ... FOR UPDATE) ate o fim da transacao
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoCozinhaOrm p where p.pedidoId = :pedidoId")
    Optional<PedidoCozinhaOrm> buscarComLock(@Param("pedidoId") Long pedidoId);

    // fila da cozinha: mais antigos primeiro
    List<PedidoCozinhaOrm> findByStatusInOrderByCriadoEmAsc(Collection<StatusPreparo> status);
}
