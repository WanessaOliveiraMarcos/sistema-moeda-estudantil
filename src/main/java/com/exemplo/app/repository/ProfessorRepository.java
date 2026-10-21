package com.exemplo.app.repository;

import com.exemplo.app.model.Professor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    long countByDepartamentoInstituicaoId(Long instituicaoId);

    boolean existsByCpf(String cpf);
}
