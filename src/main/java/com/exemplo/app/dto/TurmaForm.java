package com.exemplo.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TurmaForm {

    @NotBlank(message = "Informe o nome da turma")
    private String nome;

    @NotBlank(message = "Informe o semestre")
    @Pattern(regexp = "\\d{4}/[12]", message = "Use o formato AAAA/S, ex.: 2026/2")
    private String semestre;
}
