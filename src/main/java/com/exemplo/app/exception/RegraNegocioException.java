package com.exemplo.app.exception;

import lombok.Getter;

/** Violação de regra de negócio ligada a um campo do formulário. */
@Getter
public class RegraNegocioException extends RuntimeException {

    private final String campo;

    public RegraNegocioException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }
}
