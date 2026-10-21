package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Pedido de um aluno para enviar moedas a um colega da mesma turma.
 * As moedas só mudam de dono quando o professor da turma aprova.
 */
@Entity
@Table(name = "transferencia_aluno")
@Getter @Setter @NoArgsConstructor
public class TransferenciaAluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    @ManyToOne(optional = false)
    @JoinColumn(name = "remetente_id")
    private Aluno remetente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destinatario_id")
    private Aluno destinatario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "criterio_id")
    private CriterioTroca criterio;

    @Column(nullable = false)
    private Integer valor;

    @Column(length = 500)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusTransferencia status = StatusTransferencia.PENDENTE;

    // momento do pedido
    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    // momento da aprovação ou recusa
    private LocalDateTime decididaEm;

    private String motivoRecusa;
}
