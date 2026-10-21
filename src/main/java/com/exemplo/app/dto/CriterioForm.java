package com.exemplo.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CriterioForm {

    @NotBlank(message = "Descreva o critério")
    private String descricao;

    @NotNull(message = "Informe o máximo de moedas")
    @Positive(message = "O máximo deve ser maior que zero")
    private Integer valorMaximo;
}
