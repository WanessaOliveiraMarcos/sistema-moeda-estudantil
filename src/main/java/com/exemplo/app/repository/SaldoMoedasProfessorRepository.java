package com.exemplo.app.repository;

import com.exemplo.app.model.SaldoMoedasProfessor;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SaldoMoedasProfessorRepository extends JpaRepository<SaldoMoedasProfessor, Long> {

    List<SaldoMoedasProfessor> findBySemestre(String semestre);

    Optional<SaldoMoedasProfessor> findByProfessorIdAndSemestre(Long professorId, String semestre);

    // semestre AAAA/S ordena corretamente como texto
    Optional<SaldoMoedasProfessor> findTopByProfessorIdOrderBySemestreDesc(Long professorId);

    // trava a linha até o fim da transação: dois envios simultâneos não gastam o mesmo saldo
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SaldoMoedasProfessor s where s.professor.id = :professorId and s.semestre = :semestre")
    Optional<SaldoMoedasProfessor> buscarParaAtualizar(Long professorId, String semestre);
}
