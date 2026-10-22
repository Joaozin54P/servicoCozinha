package com.example.servicocozinha.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.servicocozinha.entity.ItemCozinha;
import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.exception.PedidoJaRecebidoException;
import com.example.servicocozinha.exception.RegraDeNegocioException;
import com.example.servicocozinha.repository.PedidoCozinhaRepository;

// So recebe pedido aprovado e coloca na fila (NA_FILA).
// E idempotente: o RabbitMQ entrega "pelo menos uma vez", entao a mesma mensagem pode chegar de novo.
// O pedido repetido NAO e duplicado e NAO volta para a fila (nao desfaz o preparo que ja comecou).
// Nao e @Transactional de proposito: a violacao da restricao unica precisa ser tratada FORA da transacao do salvar.
public class ReceberPedidoService {

    private final PedidoCozinhaRepository repository;

    public ReceberPedidoService(PedidoCozinhaRepository repository) {
        this.repository = repository;
    }

    public ResultadoRecebimento receber(NovoPedidoCozinha novo) {
        validar(novo);

        // 1) ja chegou antes: devolve o que ja existe
        Optional<PedidoCozinha> existente = repository.buscarPorPedidoId(novo.pedidoId());
        if (existente.isPresent()) {
            return new ResultadoRecebimento(existente.get(), true);
        }

        LocalDateTime agora = LocalDateTime.now();
        PedidoCozinha pedido = new PedidoCozinha(
                null,
                novo.pedidoId(),
                novo.senhaRetirada(),
                novo.clienteId(),
                novo.observacao(),
                StatusPreparo.NA_FILA,
                novo.itens().stream().map(ReceberPedidoService::montarItem).toList(),
                novo.criadoEm() == null ? agora : novo.criadoEm(),
                agora,
                agora);

        try {
            return new ResultadoRecebimento(repository.salvar(pedido), false);

        } catch (PedidoJaRecebidoException e) {
            // 2) a mesma mensagem chegou DUAS VEZES AO MESMO TEMPO: as duas passaram pela checagem acima,
            // mas o banco (restricao unica no pedidoId) deixou so uma gravar. A outra devolve a que ganhou.
            PedidoCozinha vencedor = repository.buscarPorPedidoId(novo.pedidoId()).orElseThrow(() -> e);
            return new ResultadoRecebimento(vencedor, true);
        }
    }

    private static void validar(NovoPedidoCozinha novo) {
        if (novo == null || novo.pedidoId() == null) {
            throw new RegraDeNegocioException("Informe o pedidoId");
        }
        if (novo.itens() == null || novo.itens().isEmpty()) {
            throw new RegraDeNegocioException("O pedido precisa ter pelo menos um item");
        }
        for (NovoPedidoCozinha.Item item : novo.itens()) {
            if (item.nomeProduto() == null || item.nomeProduto().isBlank()) {
                throw new RegraDeNegocioException("Informe o nome do produto de cada item");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new RegraDeNegocioException("A quantidade de cada item deve ser maior que zero");
            }
        }
    }

    private static ItemCozinha montarItem(NovoPedidoCozinha.Item item) {
        List<String> adicionais = item.adicionais() == null ? List.of() : List.copyOf(item.adicionais());
        return new ItemCozinha(null, item.nomeProduto().trim(), item.quantidade(), adicionais, item.observacao());
    }
}
