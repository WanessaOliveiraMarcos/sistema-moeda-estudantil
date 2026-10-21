package com.exemplo.app.repository;

import com.exemplo.app.model.Aluno;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    // filtros nulos são ignorados
    @Query("""
            select a from Aluno a
            where (:instituicaoId is null or a.instituicao.id = :instituicaoId)
              and (:busca is null
                   or lower(a.nome) like lower(concat('%', :busca, '%'))
                   or lower(a.email) like lower(concat('%', :busca, '%'))
                   or a.cpf like concat('%', :busca, '%'))
            order by a.nome
            """)
    List<Aluno> buscar(Long instituicaoId, String busca);

    long countByInstituicaoId(Long instituicaoId);

    // trava a linha até o fim da transação: evita gastar o mesmo saldo em dois resgates simultâneos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Aluno a where a.id = :id")
    Optional<Aluno> buscarParaAtualizar(Long id);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);
}
