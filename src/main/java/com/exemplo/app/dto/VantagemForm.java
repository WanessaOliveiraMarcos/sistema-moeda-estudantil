package com.exemplo.app.dto;

import com.exemplo.app.model.Vantagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class VantagemForm {

    private Long id;

    @NotBlank(message = "Informe o nome")
    private String nome;

    @NotBlank(message = "Informe a descrição")
    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @NotNull(message = "Informe o custo em moedas")
    @Positive(message = "O custo deve ser maior que zero")
    private Integer valor;

    // nova foto enviada; obrigatória no cadastro, opcional na edição
    private MultipartFile arquivo;

    // caminho da foto atual no storage, só para exibição na edição
    private String fotoAtual;

    public static VantagemForm de(Vantagem vantagem) {
        VantagemForm form = new VantagemForm();
        form.setId(vantagem.getId());
        form.setNome(vantagem.getNome());
        form.setDescricao(vantagem.getDescricao());
        form.setValor(vantagem.getValor());
        form.setFotoAtual(vantagem.getFoto());
        return form;
    }
}
