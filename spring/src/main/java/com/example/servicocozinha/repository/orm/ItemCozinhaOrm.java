package com.example.servicocozinha.repository.orm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "item_cozinha")
public class ItemCozinhaOrm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeProduto;

    private Integer quantidade;

    // adicionais separados por "|" (ver CozinhaRepositoryAdapter)
    @Column(length = 500)
    private String adicionais;

    @Column(length = 500)
    private String observacao;

    public ItemCozinhaOrm() {
    }

    public ItemCozinhaOrm(Long id, String nomeProduto, Integer quantidade, String adicionais, String observacao) {
        this.id = id;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.adicionais = adicionais;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public String getAdicionais() {
        return adicionais;
    }

    public String getObservacao() {
        return observacao;
    }
}
