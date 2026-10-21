package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dados de acesso comuns a aluno, professor e empresa parceira.
 * Com JOINED, cada subclasse tem sua própria tabela ligada a "usuario" pelo id.
 */
@Entity
@Table(name = "usuario")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter @Setter @NoArgsConstructor
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // login
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    // implementados pelas subclasses; não são colunas de "usuario"
    public abstract String getNome();

    public abstract Perfil getPerfil();
}
