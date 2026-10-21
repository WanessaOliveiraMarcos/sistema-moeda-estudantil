package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "aluno")
@Getter @Setter @NoArgsConstructor
public class Aluno extends Usuario {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private String rg;

    @Column(nullable = false)
    private String endereco;

    @ManyToOne(optional = false)
    @JoinColumn(name = "instituicao_id")
    private Instituicao instituicao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id")
    private Curso curso;

    @Column(nullable = false)
    private Integer saldoMoedas = 0;

    @Override
    public Perfil getPerfil() {
        return Perfil.ALUNO;
    }
}
