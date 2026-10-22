package com.example.servicocozinha.repository.adapter;

import java.util.List;

import com.example.servicocozinha.entity.ItemCozinha;
import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.repository.orm.ItemCozinhaOrm;
import com.example.servicocozinha.repository.orm.PedidoCozinhaOrm;

public class CozinhaRepositoryAdapter {

    private static final String SEPARADOR = "|";

    private CozinhaRepositoryAdapter() {
    }

    public static PedidoCozinha castOrm(PedidoCozinhaOrm orm) {
        List<ItemCozinha> itens = orm.getItens().stream()
                .map(CozinhaRepositoryAdapter::castOrm)
                .toList();

        return new PedidoCozinha(
                orm.getId(),
                orm.getPedidoId(),
                orm.getSenhaRetirada(),
                orm.getClienteId(),
                orm.getObservacao(),
                orm.getStatus(),
                itens,
                orm.getCriadoEm(),
                orm.getRecebidoEm(),
                orm.getAtualizadoEm());
    }

    public static PedidoCozinhaOrm castEntity(PedidoCozinha entity) {
        List<ItemCozinhaOrm> itens = entity.itens().stream()
                .map(CozinhaRepositoryAdapter::castEntity)
                .toList();

        return new PedidoCozinhaOrm(
                entity.id(),
                entity.pedidoId(),
                entity.senhaRetirada(),
                entity.clienteId(),
                entity.observacao(),
                entity.status(),
                itens,
                entity.criadoEm(),
                entity.recebidoEm(),
                entity.atualizadoEm());
    }

    private static ItemCozinha castOrm(ItemCozinhaOrm orm) {
        return new ItemCozinha(
                orm.getId(),
                orm.getNomeProduto(),
                orm.getQuantidade(),
                separar(orm.getAdicionais()),
                orm.getObservacao());
    }

    private static ItemCozinhaOrm castEntity(ItemCozinha entity) {
        return new ItemCozinhaOrm(
                entity.id(),
                entity.nomeProduto(),
                entity.quantidade(),
                String.join(SEPARADOR, entity.adicionais()),
                entity.observacao());
    }

    private static List<String> separar(String adicionais) {
        if (adicionais == null || adicionais.isBlank()) {
            return List.of();
        }
        return List.of(adicionais.split("\\|"));
    }
}
