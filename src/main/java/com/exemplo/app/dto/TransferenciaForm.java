package com.exemplo.app.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Pedido de envio de moedas a um colega da mesma turma. */
@Getter @Setter
public class TransferenciaForm {

    @NotNull(message = "Selecione a turma")
    private Long turmaId;

    @NotNull(message = "Selecione o colega")
    private Long destinatarioId;

    @NotNull(message = "Selecione o critério")
    private Long criterioId;

    @NotNull(message = "Informe a quantidade de moedas")
    @Positive(message = "A quantidade deve ser maior que zero")
    private Integer valor;

    @Size(max = 500, message = "A mensagem deve ter no máximo 500 caracteres")
    private String mensagem;
}
