package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "troca_vantagem")
@Getter @Setter @NoArgsConstructor
public class TrocaVantagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id")
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vantagem_id")
    private Vantagem vantagem;

    // valor pago no momento da troca (o preço da vantagem pode mudar depois)
    @Column(nullable = false)
    private Integer valor;

    // código enviado no e-mail de cupom para conferência
    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();
}
