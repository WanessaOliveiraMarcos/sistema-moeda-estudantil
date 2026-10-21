package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter @Setter @NoArgsConstructor
public class Administrador extends Usuario {

    @Column(nullable = false)
    private String nome;

    @Override
    public Perfil getPerfil() {
        return Perfil.ADMIN;
    }
}
