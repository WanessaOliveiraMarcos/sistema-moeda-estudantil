package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vantagem")
@Getter @Setter @NoArgsConstructor
public class Vantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 1000)
    private String descricao;

    // caminho relativo ao storage, ex.: "vantagens/3f2a...png" (servido em /storage/**)
    @Column(nullable = false)
    private String foto;

    // custo em moedas
    @Column(nullable = false)
    private Integer valor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "empresa_id")
    private EmpresaParceira empresa;
}
