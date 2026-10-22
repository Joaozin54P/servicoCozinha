# servicoCozinha

Cozinha da lanchonete: **recebe os pedidos aprovados** do servicoPedido, mantém a **fila de preparo** e avisa o servicoPedido quando a cozinha começa (`EM_PREPARO`) e termina (`PRONTO`) um pedido.

## Tecnologias
Java 21 · Spring Boot 4.1.1 · JPA · banco H2 em memória · RabbitMQ (opcional) · Maven

## Porta
`8084` (pelo gateway: `/cozinha/**` em `http://localhost:8080`)

## Estrutura (2 módulos, igual aos outros serviços)
```
servicoCozinha/
├── domain/   REGRAS DE NEGÓCIO: Java puro, sem Spring
│   └── entity/ (PedidoCozinha, ItemCozinha, StatusPreparo)  service/  repository/ (interface)
│       messaging/ + event/ (avisos)  exception/
└── spring/   controller/ (API REST + DTOs)  repository/ (JPA + H2)  messaging/ (RabbitMQ ou log)  config/
```

## Fluxo
`NA_FILA → EM_PREPARO → PRONTO` (ou `CANCELADO`, quando o cliente cancela no servicoPedido)

1. O servicoPedido cria o pedido e publica em `pedido-criado`. A cozinha recebe e coloca `NA_FILA`.
2. A cozinha chama `POST /cozinha/pedidos/{id}/iniciar` → `EM_PREPARO` e publica em `pedido-status-cozinha`.
3. `POST /cozinha/pedidos/{id}/pronto` → `PRONTO` e publica de novo. O servicoPedido atualiza o pedido dele.
4. A retirada pelo cliente continua sendo no servicoPedido (`POST /pedidos/{id}/retirar`).

"Pedido aprovado" = pedido criado e aceito pelo servicoPedido (o pagamento é simulado, então não há etapa de aprovação separada).

- **Idempotente:** o RabbitMQ pode entregar a mesma mensagem duas vezes. O pedido repetido não duplica e não volta para a fila (restrição única em `pedido_id`).
- **Cancelamento:** `pedido-cancelado` tira da fila (`NA_FILA` ou `EM_PREPARO`). Pedido já `PRONTO` não cancela.
- Mudanças de status travam a linha do pedido (`SELECT ... FOR UPDATE`).

## Como rodar
```bash
mvn clean install
cd spring
mvn spring-boot:run
```
Console do H2: http://localhost:8084/h2-console (`jdbc:h2:mem:cozinhadb`, usuário `sa`, senha vazia).

### Mensageria (RabbitMQ), desligada por padrão
Controlada por `MENSAGERIA_HABILITADA`.
- **`false` (padrão):** sem RabbitMQ. Os pedidos entram por `POST /cozinha/pedidos` e as mudanças de status só aparecem no log. **O servicoPedido não é avisado** nesse modo.
- **`true`:** escuta `pedido-criado` e `pedido-cancelado` e publica `pedido-status-cozinha`. Use o **mesmo** RabbitMQ do servicoPedido, com variáveis de ambiente (sem gravar senha no repositório): `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`, `RABBITMQ_VHOST`, `RABBITMQ_SSL`.

## Segurança
Este serviço **não tem Spring Security**: o gateway só deixa passar `COZINHA` e `ADMIN` (e só `ADMIN` no `POST /cozinha/pedidos`). Não exponha a porta 8084 diretamente.

## Rotas (sempre pelo gateway)
| Rota | Quem acessa | Resposta |
| --- | --- | --- |
| `GET /cozinha/pedidos` | `COZINHA`/`ADMIN` | fila de trabalho (`NA_FILA` + `EM_PREPARO`), mais antigos primeiro |
| `GET /cozinha/pedidos?status=PRONTO` | `COZINHA`/`ADMIN` | só desse status (`NA_FILA`, `EM_PREPARO`, `PRONTO`, `CANCELADO`) |
| `GET /cozinha/pedidos/{pedidoId}` | `COZINHA`/`ADMIN` | um pedido |
| `POST /cozinha/pedidos/{pedidoId}/iniciar` | `COZINHA`/`ADMIN` | `EM_PREPARO` |
| `POST /cozinha/pedidos/{pedidoId}/pronto` | `COZINHA`/`ADMIN` | `PRONTO` |
| `POST /cozinha/pedidos` | só `ADMIN` (testes) | `201` novo, `200` se já existia |

`{pedidoId}` é o ID do pedido no servicoPedido. Erros: `{"erro": "..."}` com 400 (dado inválido ou transição não permitida), 404 (pedido que a cozinha não conhece).

Corpo do `POST /cozinha/pedidos` (mesmo JSON da fila `pedido-criado`):
```json
{ "pedidoId": 1, "senhaRetirada": "001", "clienteId": 5, "observacao": null,
  "criadoEm": "2026-10-01T12:00:00",
  "itens": [ { "nomeProduto": "Burger Bacon", "quantidade": 2, "adicionais": ["Bacon extra"], "observacao": "sem cebola" } ] }
```

## Contrato das filas
| Fila | Quem publica | Quem lê |
| --- | --- | --- |
| `pedido-criado` | servicoPedido | **servicoCozinha** |
| `pedido-cancelado` | servicoPedido | **servicoCozinha** |
| `pedido-status-cozinha` | **servicoCozinha** | servicoPedido |

## Testes
`domain` (sem Spring, sem banco, sem RabbitMQ): `cd domain && mvn test`. Cobre recebimento, mensagem repetida/simultânea, ciclo de status, cancelamento e ordem da fila.
`spring`: `ServicoCozinhaApplicationTests` verifica se o contexto sobe.

Para testar na mão use o arquivo `requests.http`.
