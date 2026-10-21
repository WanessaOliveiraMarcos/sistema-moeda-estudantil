package com.exemplo.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EnvioMoedasForm {

    @NotNull(message = "Selecione o aluno")
    private Long alunoId;

    @NotNull(message = "Informe a quantidade de moedas")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer valor;

    // mensagem aberta e obrigatória
    @NotBlank(message = "Informe o motivo do reconhecimento")
    @Size(max = 500, message = "O motivo deve ter no máximo 500 caracteres")
    private String motivo;
}
