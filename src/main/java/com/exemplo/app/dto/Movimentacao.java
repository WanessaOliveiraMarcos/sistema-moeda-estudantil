package com.exemplo.app.dto;

import java.time.LocalDateTime;

/**
 * Linha do extrato. valor é positivo para entradas e negativo para saídas;
 * codigo só existe em trocas de vantagem.
 */
public record Movimentacao(LocalDateTime dataHora, String tipo, String descricao, String detalhe,
                           int valor, String codigo) {
}
