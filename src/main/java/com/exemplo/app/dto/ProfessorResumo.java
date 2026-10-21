package com.exemplo.app.dto;

import com.exemplo.app.model.Professor;

/** Professor com o saldo do semestre atual. */
public record ProfessorResumo(Professor professor, Integer saldo) {
}
