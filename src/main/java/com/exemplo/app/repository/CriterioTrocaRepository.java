package com.exemplo.app.repository;

import com.exemplo.app.model.CriterioTroca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CriterioTrocaRepository extends JpaRepository<CriterioTroca, Long> {

    Optional<CriterioTroca> findByIdAndTurmaId(Long id, Long turmaId);
}
