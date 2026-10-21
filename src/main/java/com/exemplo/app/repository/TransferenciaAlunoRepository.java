package com.exemplo.app.repository;

import com.exemplo.app.model.StatusTransferencia;
import com.exemplo.app.model.TransferenciaAluno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TransferenciaAlunoRepository extends JpaRepository<TransferenciaAluno, Long> {

    List<TransferenciaAluno> findByTurmaProfessorIdAndStatusOrderByDataHoraAsc(Long professorId,
                                                                             StatusTransferencia status);

    List<TransferenciaAluno> findTop20ByTurmaProfessorIdAndStatusNotOrderByDecididaEmDesc(Long professorId,
                                                                                         StatusTransferencia status);

    long countByTurmaProfessorIdAndStatus(Long professorId, StatusTransferencia status);

    @Query("""
            select t from TransferenciaAluno t
            where t.remetente.id = :alunoId or t.destinatario.id = :alunoId
            order by t.dataHora desc
            """)
    List<TransferenciaAluno> buscarDoAluno(Long alunoId);

    List<TransferenciaAluno> findByRemetenteIdAndStatus(Long alunoId, StatusTransferencia status);

    List<TransferenciaAluno> findByDestinatarioIdAndStatus(Long alunoId, StatusTransferencia status);

    // trava o pedido: dois cliques em "Aprovar" não transferem duas vezes
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from TransferenciaAluno t where t.id = :id")
    Optional<TransferenciaAluno> buscarParaAtualizar(Long id);
}
