package com.exemplo.app.dto;

import com.exemplo.app.model.Aluno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

@Getter @Setter
public class AlunoForm {

    private Long id;

    @NotBlank(message = "Informe o nome")
    private String nome;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Informe o CPF")
    @CPF(message = "CPF inválido")
    private String cpf;

    @NotBlank(message = "Informe o RG")
    private String rg;

    @NotBlank(message = "Informe o endereço")
    private String endereco;

    @NotNull(message = "Selecione a instituição")
    private Long instituicaoId;

    @NotNull(message = "Selecione o curso")
    private Long cursoId;

    // obrigatória no cadastro; em branco na edição mantém a atual
    private String senha;

    public static AlunoForm de(Aluno aluno) {
        AlunoForm form = new AlunoForm();
        form.setId(aluno.getId());
        form.setNome(aluno.getNome());
        form.setEmail(aluno.getEmail());
        form.setCpf(aluno.getCpf());
        form.setRg(aluno.getRg());
        form.setEndereco(aluno.getEndereco());
        form.setInstituicaoId(aluno.getInstituicao().getId());
        form.setCursoId(aluno.getCurso().getId());
        return form;
    }
}
