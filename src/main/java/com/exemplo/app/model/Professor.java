package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "professor")
@Getter @Setter @NoArgsConstructor
public class Professor extends Usuario {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    // a instituição do professor é a do departamento
    @ManyToOne(optional = false)
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    @Override
    public Perfil getPerfil() {
        return Perfil.PROFESSOR;
    }
}
