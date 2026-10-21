package com.exemplo.app.dto;

import com.exemplo.app.model.EmpresaParceira;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class EmpresaParceiraForm {

    private Long id;

    @NotBlank(message = "Informe o nome da empresa")
    private String nome;

    @NotBlank(message = "Informe o responsável")
    private String responsavel;

    @NotBlank(message = "Informe o e-mail")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Informe o telefone")
    private String telefone;

    @NotBlank(message = "Informe o endereço")
    private String endereco;

    // obrigatória no cadastro; em branco na edição mantém a atual
    private String senha;

    public static EmpresaParceiraForm de(EmpresaParceira empresa) {
        EmpresaParceiraForm form = new EmpresaParceiraForm();
        form.setId(empresa.getId());
        form.setNome(empresa.getNome());
        form.setResponsavel(empresa.getResponsavel());
        form.setEmail(empresa.getEmail());
        form.setTelefone(empresa.getTelefone());
        form.setEndereco(empresa.getEndereco());
        return form;
    }
}
