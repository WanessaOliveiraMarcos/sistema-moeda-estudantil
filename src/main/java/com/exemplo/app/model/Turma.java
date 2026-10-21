package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Relação entre professor e alunos. Um aluno pode estar em várias turmas e um professor ter várias. */
@Entity
@Table(name = "turma")
@Getter @Setter @NoArgsConstructor
public class Turma {

    public static final int LIMITE_ALUNOS = 40;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ex.: "Laboratório de Desenvolvimento de Software"
    @Column(nullable = false)
    private String nome;

    // AAAA/S, ex.: "2026/2"
    @Column(nullable = false, length = 6)
    private String semestre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToMany
    @JoinTable(name = "matricula",
            joinColumns = @JoinColumn(name = "turma_id"),
            inverseJoinColumns = @JoinColumn(name = "aluno_id"))
    private Set<Aluno> alunos = new HashSet<>();

    @OneToMany(mappedBy = "turma", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("valorMaximo, descricao")
    private List<CriterioTroca> criterios = new ArrayList<>();

    public int getVagas() {
        return LIMITE_ALUNOS - alunos.size();
    }

    public boolean isLotada() {
        return getVagas() <= 0;
    }

    public boolean temAluno(Long alunoId) {
        return alunos.stream().anyMatch(a -> a.getId().equals(alunoId));
    }
}
