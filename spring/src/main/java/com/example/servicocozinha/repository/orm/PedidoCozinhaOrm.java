package com.example.servicocozinha.repository.orm;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.servicocozinha.entity.StatusPreparo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
// restricao unica: o mesmo pedido nao entra duas vezes na cozinha. E o "lock" do recebimento:
// mesmo com duas mensagens iguais ao mesmo tempo, o banco so deixa uma gravar.
@Table(name = "pedido_cozinha", uniqueConstraints = @UniqueConstraint(
        name = "uk_pedido_cozinha_pedido", columnNames = {"pedido_id"}))
public class PedidoCozinhaOrm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID do pedido no servicoPedido
    @Column(name = "pedido_id", nullable = false)
    private Long pedidoId;

    private String senhaRetirada;

    private Long clienteId;

    @Column(length = 500)
    private String observacao;

    @Enumerated(EnumType.STRING)
    private StatusPreparo status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_cozinha_id")
    private List<ItemCozinhaOrm> itens = new ArrayList<>();

    private LocalDateTime criadoEm;

    private LocalDateTime recebidoEm;

    private LocalDateTime atualizadoEm;

    public PedidoCozinhaOrm() {
    }

    public PedidoCozinhaOrm(Long id, Long pedidoId, String senhaRetirada, Long clienteId, String observacao,
                            StatusPreparo status, List<ItemCozinhaOrm> itens, LocalDateTime criadoEm,
                            LocalDateTime recebidoEm, LocalDateTime atualizadoEm) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.senhaRetirada = senhaRetirada;
        this.clienteId = clienteId;
        this.observacao = observacao;
        this.status = status;
        this.itens = new ArrayList<>(itens);
        this.criadoEm = criadoEm;
        this.recebidoEm = recebidoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public String getSenhaRetirada() {
        return senhaRetirada;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public String getObservacao() {
        return observacao;
    }

    public StatusPreparo getStatus() {
        return status;
    }

    public List<ItemCozinhaOrm> getItens() {
        return itens;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getRecebidoEm() {
        return recebidoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
