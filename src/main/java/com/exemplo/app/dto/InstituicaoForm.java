package com.exemplo.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.util.ArrayList;
import java.util.List;

/** Cadastro de instituição parceira pelo admin, já com cursos e a lista de professores. */
@Getter @Setter
public class InstituicaoForm {

    @NotBlank(message = "Informe o nome")
    private String nome;

    @NotBlank(message = "Informe o endereço")
    private String endereco;

    @Valid
    @NotEmpty(message = "Cadastre pelo menos um curso")
    private List<CursoLinha> cursos = new ArrayList<>();

    @Valid
    @NotEmpty(message = "Cadastre pelo menos um professor")
    private List<ProfessorLinha> professores = new ArrayList<>();

    @Getter @Setter
    public static class CursoLinha {

        @NotBlank(message = "Informe o curso")
        private String nome;

        @NotNull(message = "Informe os semestres")
        @Min(value = 1, message = "Mínimo de 1 semestre")
        @Max(value = 20, message = "Máximo de 20 semestres")
        private Integer semestres;
    }

    @Getter @Setter
    public static class ProfessorLinha {

        @NotBlank(message = "Informe o nome")
        private String nome;

        @NotBlank(message = "Informe o CPF")
        @CPF(message = "CPF inválido")
        private String cpf;

        @NotBlank(message = "Informe o e-mail")
        @Email(message = "E-mail inválido")
        private String email;

        // departamentos com o mesmo nome são criados uma vez só
        @NotBlank(message = "Informe o departamento")
        private String departamento;

        // senha inicial de acesso do professor
        @NotBlank(message = "Informe a senha inicial")
        @Size(min = 6, message = "Mínimo de 6 caracteres")
        private String senha;
    }
}
