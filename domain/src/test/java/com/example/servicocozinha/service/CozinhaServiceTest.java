package com.example.servicocozinha.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.event.CozinhaEvent;
import com.example.servicocozinha.event.StatusPreparoAlteradoEvent;
import com.example.servicocozinha.exception.NaoEncontradoException;
import com.example.servicocozinha.exception.PedidoJaRecebidoException;
import com.example.servicocozinha.exception.RegraDeNegocioException;
import com.example.servicocozinha.repository.PedidoCozinhaRepository;

// Testa as regras da cozinha sem Spring, sem banco e sem RabbitMQ (repositorio e avisos sao de mentira).
class CozinhaServiceTest {

    private RepositorioEmMemoria repositorio;
    private List<CozinhaEvent> avisos;
    private ReceberPedidoService receber;
    private AtualizarPreparoService preparo;
    private ConsultarFilaService fila;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEmMemoria();
        avisos = new ArrayList<>();
        receber = new ReceberPedidoService(repositorio);
        preparo = new AtualizarPreparoService(repositorio, avisos::add);
        fila = new ConsultarFilaService(repositorio);
    }

    // ---------- receber ----------

    @Test
    void pedidoAprovadoEntraNaFila() {
        ResultadoRecebimento resultado = receber.receber(pedido(1L));

        assertFalse(resultado.repetido());
        assertEquals(StatusPreparo.NA_FILA, resultado.pedido().status());
        assertEquals("001", resultado.pedido().senhaRetirada());
        assertEquals(1, resultado.pedido().itens().size());
        assertEquals("Burger Bacon", resultado.pedido().itens().get(0).nomeProduto());
    }

    @Test
    void mesmoPedidoChegandoDeNovoNaoDuplicaENaoVoltaParaAFila() {
        receber.receber(pedido(1L));
        preparo.iniciar(1L);

        ResultadoRecebimento segunda = receber.receber(pedido(1L));

        assertTrue(segunda.repetido());
        assertEquals(StatusPreparo.EM_PREPARO, segunda.pedido().status());
        assertEquals(1, repositorio.total());
    }

    @Test
    void duasMensagensAoMesmoTempoGravamUmPedidoSo() {
        // simula a corrida: a checagem inicial nao enxerga o pedido, mas o banco recusa a gravacao duplicada
        receber.receber(pedido(7L));
        repositorio.fingirQueAPrimeiraBuscaNaoEnxerga = true;

        ResultadoRecebimento resultado = receber.receber(pedido(7L));

        assertTrue(resultado.repetido());
        assertEquals(1, repositorio.total());
    }

    @Test
    void pedidoSemItensERecusado() {
        NovoPedidoCozinha semItens = new NovoPedidoCozinha(1L, "001", 5L, null, null, List.of());

        assertThrows(RegraDeNegocioException.class, () -> receber.receber(semItens));
    }

    @Test
    void pedidoSemIdERecusado() {
        assertThrows(RegraDeNegocioException.class, () -> receber.receber(pedido(null)));
    }

    @Test
    void quantidadeZeroERecusada() {
        NovoPedidoCozinha ruim = new NovoPedidoCozinha(1L, "001", 5L, null, null,
                List.of(new NovoPedidoCozinha.Item("Burger", 0, List.of(), null)));

        assertThrows(RegraDeNegocioException.class, () -> receber.receber(ruim));
    }

    // ---------- preparo ----------

    @Test
    void cicloCompletoNaFilaEmPreparoPronto() {
        receber.receber(pedido(1L));

        assertEquals(StatusPreparo.EM_PREPARO, preparo.iniciar(1L).status());
        assertEquals(StatusPreparo.PRONTO, preparo.marcarPronto(1L).status());

        // cada mudanca avisa quem escuta (la na camada spring, vira mensagem para o servicoPedido)
        assertEquals(2, avisos.size());
        StatusPreparoAlteradoEvent primeiro = (StatusPreparoAlteradoEvent) avisos.get(0);
        assertEquals(1L, primeiro.pedidoId());
        assertEquals(StatusPreparo.NA_FILA, primeiro.statusAnterior());
        assertEquals(StatusPreparo.EM_PREPARO, primeiro.novoStatus());
        assertEquals(StatusPreparo.PRONTO, ((StatusPreparoAlteradoEvent) avisos.get(1)).novoStatus());
    }

    @Test
    void naoPodeFicarProntoSemTerComecado() {
        receber.receber(pedido(1L));

        assertThrows(RegraDeNegocioException.class, () -> preparo.marcarPronto(1L));
        assertEquals(StatusPreparo.NA_FILA, fila.buscar(1L).status());
        assertTrue(avisos.isEmpty());
    }

    @Test
    void naoPodeComecarDuasVezes() {
        receber.receber(pedido(1L));
        preparo.iniciar(1L);

        assertThrows(RegraDeNegocioException.class, () -> preparo.iniciar(1L));
    }

    @Test
    void pedidoInexistenteNaoEncontrado() {
        assertThrows(NaoEncontradoException.class, () -> preparo.iniciar(99L));
        assertThrows(NaoEncontradoException.class, () -> fila.buscar(99L));
    }

    // ---------- cancelamento (aviso vindo do servicoPedido) ----------

    @Test
    void cancelamentoTiraDaFila() {
        receber.receber(pedido(1L));

        assertEquals(StatusPreparo.CANCELADO, preparo.cancelar(1L).status());
        assertTrue(fila.filaAtiva().isEmpty());
    }

    @Test
    void cancelamentoQueChegaDepoisDoComecoDoPreparoTambemCancela() {
        receber.receber(pedido(1L));
        preparo.iniciar(1L);

        assertEquals(StatusPreparo.CANCELADO, preparo.cancelar(1L).status());
    }

    @Test
    void pedidoProntoNaoPodeSerCancelado() {
        receber.receber(pedido(1L));
        preparo.iniciar(1L);
        preparo.marcarPronto(1L);

        assertThrows(RegraDeNegocioException.class, () -> preparo.cancelar(1L));
    }

    @Test
    void pedidoCanceladoNaoVoltaParaOPreparo() {
        receber.receber(pedido(1L));
        preparo.cancelar(1L);

        assertThrows(RegraDeNegocioException.class, () -> preparo.iniciar(1L));
    }

    // ---------- fila ----------

    @Test
    void filaAtivaMostraSoNaFilaEEmPreparoDoMaisAntigoParaOMaisNovo() {
        receber.receber(pedido(1L));
        receber.receber(pedido(2L));
        receber.receber(pedido(3L));
        receber.receber(pedido(4L));
        preparo.iniciar(2L);                 // em preparo
        preparo.iniciar(3L);
        preparo.marcarPronto(3L);            // pronto: sai da fila ativa
        preparo.cancelar(4L);                // cancelado: sai da fila ativa

        List<Long> ids = fila.filaAtiva().stream().map(PedidoCozinha::pedidoId).toList();

        assertEquals(List.of(1L, 2L), ids);
        assertEquals(List.of(3L), fila.listarPorStatus(StatusPreparo.PRONTO).stream()
                .map(PedidoCozinha::pedidoId).toList());
    }

    // ---------- apoio ----------

    private static NovoPedidoCozinha pedido(Long pedidoId) {
        // criadoEm crescente com o id: pedido 1 mais antigo que o 2, e assim por diante
        LocalDateTime criadoEm = LocalDateTime.of(2026, 10, 1, 12, 0).plusMinutes(pedidoId == null ? 0 : pedidoId);
        return new NovoPedidoCozinha(pedidoId, String.format("%03d", pedidoId == null ? 0 : pedidoId), 5L,
                "sem guardanapo", criadoEm,
                List.of(new NovoPedidoCozinha.Item("Burger Bacon", 2, List.of("Bacon extra"), "sem cebola")));
    }

    // "banco" de mentira: faz o mesmo que a restricao unica do pedidoId
    private static class RepositorioEmMemoria implements PedidoCozinhaRepository {

        private final Map<Long, PedidoCozinha> porPedidoId = new LinkedHashMap<>();
        private long proximoId = 1;
        boolean fingirQueAPrimeiraBuscaNaoEnxerga = false;

        int total() {
            return porPedidoId.size();
        }

        @Override
        public PedidoCozinha salvar(PedidoCozinha pedido) {
            PedidoCozinha existente = porPedidoId.get(pedido.pedidoId());
            if (existente != null && !existente.id().equals(pedido.id())) {
                throw new PedidoJaRecebidoException("ja existe");
            }
            PedidoCozinha gravado = pedido.id() == null
                    ? new PedidoCozinha(proximoId++, pedido.pedidoId(), pedido.senhaRetirada(), pedido.clienteId(),
                            pedido.observacao(), pedido.status(), pedido.itens(), pedido.criadoEm(),
                            pedido.recebidoEm(), pedido.atualizadoEm())
                    : pedido;
            porPedidoId.put(gravado.pedidoId(), gravado);
            return gravado;
        }

        @Override
        public Optional<PedidoCozinha> buscarPorPedidoId(Long pedidoId) {
            if (fingirQueAPrimeiraBuscaNaoEnxerga) {
                fingirQueAPrimeiraBuscaNaoEnxerga = false;
                return Optional.empty();
            }
            return Optional.ofNullable(porPedidoId.get(pedidoId));
        }

        @Override
        public Optional<PedidoCozinha> buscarPorPedidoIdComLock(Long pedidoId) {
            return Optional.ofNullable(porPedidoId.get(pedidoId));
        }

        @Override
        public List<PedidoCozinha> listarPorStatus(Collection<StatusPreparo> status) {
            return porPedidoId.values().stream()
                    .filter(p -> status.contains(p.status()))
                    .sorted((a, b) -> a.criadoEm().compareTo(b.criadoEm()))
                    .toList();
        }
    }
}
