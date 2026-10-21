package com.exemplo.app.repository;

import com.exemplo.app.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    Optional<Curso> findFirstByNome(String nome);
}
