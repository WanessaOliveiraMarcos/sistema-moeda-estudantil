package com.exemplo.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "empresa_parceira")
@Getter @Setter @NoArgsConstructor
public class EmpresaParceira extends Usuario {

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String responsavel;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false)
    private String endereco;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vantagem> vantagens = new ArrayList<>();

    @Override
    public Perfil getPerfil() {
        return Perfil.EMPRESA;
    }
}
