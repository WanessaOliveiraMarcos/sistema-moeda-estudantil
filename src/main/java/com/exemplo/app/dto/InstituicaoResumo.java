package com.exemplo.app.dto;

import com.exemplo.app.model.Curso;
import com.exemplo.app.model.Departamento;
import com.exemplo.app.model.Instituicao;
import com.exemplo.app.model.Professor;

import java.util.List;

public record InstituicaoResumo(Instituicao instituicao, List<Curso> cursos, List<Departamento> departamentos,
                                List<Professor> professores, long alunos) {
}
