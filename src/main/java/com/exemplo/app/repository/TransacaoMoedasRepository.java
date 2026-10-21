package com.exemplo.app.repository;

import com.exemplo.app.model.TransacaoMoedas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransacaoMoedasRepository extends JpaRepository<TransacaoMoedas, Long> {

    List<TransacaoMoedas> findByAlunoIdOrderByDataHoraDesc(Long alunoId);

    List<TransacaoMoedas> findByProfessorIdOrderByDataHoraDesc(Long professorId);
}
