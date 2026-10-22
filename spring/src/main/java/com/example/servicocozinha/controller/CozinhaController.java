package com.example.servicocozinha.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.servicocozinha.controller.adapter.CozinhaControllerAdapter;
import com.example.servicocozinha.controller.dto.request.ReceberPedidoRequest;
import com.example.servicocozinha.controller.dto.response.PedidoCozinhaResponse;
import com.example.servicocozinha.entity.PedidoCozinha;
import com.example.servicocozinha.entity.StatusPreparo;
import com.example.servicocozinha.service.AtualizarPreparoService;
import com.example.servicocozinha.service.ConsultarFilaService;
import com.example.servicocozinha.service.ReceberPedidoService;
import com.example.servicocozinha.service.ResultadoRecebimento;

// Quem pode chamar cada rota e decidido no GATEWAY (este servico nao tem Spring Security):
// tudo aqui e so para COZINHA/ADMIN, e o POST de receber pedido so para ADMIN.
@RestController
@RequestMapping("/cozinha/pedidos")
public class CozinhaController {

    private final ReceberPedidoService receberPedido;
    private final AtualizarPreparoService atualizarPreparo;
    private final ConsultarFilaService consultarFila;

    public CozinhaController(ReceberPedidoService receberPedido,
                             AtualizarPreparoService atualizarPreparo,
                             ConsultarFilaService consultarFila) {
        this.receberPedido = receberPedido;
        this.atualizarPreparo = atualizarPreparo;
        this.consultarFila = consultarFila;
    }

    // Recebe um pedido aprovado. Em producao quem entrega e o RabbitMQ (fila pedido-criado);
    // esta rota faz o mesmo por REST (serve para testar sem broker).
    //   - primeira vez ............ 201 Created + pedido na fila
    //   - mesmo pedido de novo .... 200 OK + o pedido que ja estava (nada e duplicado)
    @PostMapping
    public ResponseEntity<PedidoCozinhaResponse> receber(@RequestBody ReceberPedidoRequest request) {
        ResultadoRecebimento resultado = receberPedido.receber(CozinhaControllerAdapter.castRequest(request));

        HttpStatus status = resultado.repetido() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(CozinhaControllerAdapter.castResponse(resultado.pedido()));
    }

    // GET /cozinha/pedidos ............. a fila de trabalho: NA_FILA e EM_PREPARO, do mais antigo para o mais novo
    // GET /cozinha/pedidos?status=PRONTO  so os desse status (NA_FILA, EM_PREPARO, PRONTO ou CANCELADO)
    @GetMapping
    public List<PedidoCozinhaResponse> listar(@RequestParam(required = false) StatusPreparo status) {
        List<PedidoCozinha> pedidos = status == null
                ? consultarFila.filaAtiva()
                : consultarFila.listarPorStatus(status);

        return pedidos.stream().map(CozinhaControllerAdapter::castResponse).toList();
    }

    // {pedidoId} e o ID do pedido no servicoPedido
    @GetMapping("/{pedidoId}")
    public PedidoCozinhaResponse buscar(@PathVariable Long pedidoId) {
        return CozinhaControllerAdapter.castResponse(consultarFila.buscar(pedidoId));
    }

    // "comecei a preparar": NA_FILA -> EM_PREPARO (e avisa o servicoPedido)
    @PostMapping("/{pedidoId}/iniciar")
    public PedidoCozinhaResponse iniciar(@PathVariable Long pedidoId) {
        return CozinhaControllerAdapter.castResponse(atualizarPreparo.iniciar(pedidoId));
    }

    // "ficou pronto": EM_PREPARO -> PRONTO (e avisa o servicoPedido, que chama o cliente pela senha)
    @PostMapping("/{pedidoId}/pronto")
    public PedidoCozinhaResponse pronto(@PathVariable Long pedidoId) {
        return CozinhaControllerAdapter.castResponse(atualizarPreparo.marcarPronto(pedidoId));
    }
}
