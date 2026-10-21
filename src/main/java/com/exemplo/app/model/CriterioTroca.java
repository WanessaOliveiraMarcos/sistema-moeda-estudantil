package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Regra cadastrada pelo professor para os alunos da turma enviarem moedas entre si. */
@Entity
@Table(name = "criterio_troca")
@Getter @Setter @NoArgsConstructor
public class CriterioTroca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    // ex.: "Ajuda em exercício"
    @Column(nullable = false)
    private String descricao;

    // limite de moedas por envio neste critério
    @Column(nullable = false)
    private Integer valorMaximo;
}
