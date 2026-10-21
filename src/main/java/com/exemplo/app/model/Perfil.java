package com.exemplo.app.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Perfil {

    ALUNO("Aluno"),
    PROFESSOR("Professor"),
    EMPRESA("Empresa parceira"),
    ADMIN("Administrador");

    private final String descricao;
}
