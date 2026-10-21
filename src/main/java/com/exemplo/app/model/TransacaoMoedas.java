package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "transacao_moedas")
@Getter @Setter @NoArgsConstructor
public class TransacaoMoedas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @Column(nullable = false)
    private Integer valor;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();
}
