package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "saldo_moedas_professor",
       uniqueConstraints = @UniqueConstraint(columnNames = {"professor_id", "semestre"}))
@Getter @Setter @NoArgsConstructor
public class SaldoMoedasProfessor {

    /** Crédito de cada semestre; o que sobrou do semestre anterior é somado (US10). */
    public static final int MOEDAS_POR_SEMESTRE = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; 

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    // ex.: "2026/2"
    @Column(nullable = false, length = 6)
    private String semestre;

    @Column(nullable = false)
    private Integer valorInicial;

    @Column(nullable = false)
    private Integer valorFinal;

    /** Semestre no formato AAAA/S, ex.: "2026/2". */
    public static String semestreDe(LocalDate data) {
        return data.getYear() + "/" + (data.getMonthValue() <= 6 ? 1 : 2);
    }

    public static String semestreAtual() {
        return semestreDe(LocalDate.now());
    }
}
