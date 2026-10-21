package com.exemplo.app.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusTransferencia {

    PENDENTE("Aguardando o professor"),
    APROVADA("Aprovada"),
    RECUSADA("Recusada");

    private final String descricao;
}
