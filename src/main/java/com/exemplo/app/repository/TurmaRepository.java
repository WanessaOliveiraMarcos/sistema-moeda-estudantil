package com.exemplo.app.repository;

import com.exemplo.app.model.Turma;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TurmaRepository extends JpaRepository<Turma, Long> {

    List<Turma> findByProfessorIdOrderBySemestreDescNomeAsc(Long professorId);

    Optional<Turma> findByIdAndProfessorId(Long id, Long professorId);

    @Query("select t from Turma t join t.alunos a where a.id = :alunoId order by t.semestre desc, t.nome")
    List<Turma> buscarDoAluno(Long alunoId);

    // turmas da instituição do aluno em que ele ainda não está matriculado
    @Query("""
            select t from Turma t
            where t.professor.departamento.instituicao.id = :instituicaoId
              and t not in (select t2 from Turma t2 join t2.alunos a where a.id = :alunoId)
            order by t.semestre desc, t.nome
            """)
    List<Turma> buscarDisponiveis(Long instituicaoId, Long alunoId);

    boolean existsByProfessorIdAndAlunos_Id(Long professorId, Long alunoId);

    // trava a turma durante a matrícula: duas matrículas simultâneas não passam do limite de 40
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Turma t where t.id = :id")
    Optional<Turma> buscarParaAtualizar(Long id);
}
