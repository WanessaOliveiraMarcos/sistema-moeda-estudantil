package com.exemplo.app.repository;

import com.exemplo.app.model.Vantagem;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VantagemRepository extends JpaRepository<Vantagem, Long> {

    List<Vantagem> findByEmpresaIdOrderByNomeAsc(Long empresaId);

    Optional<Vantagem> findByIdAndEmpresaId(Long id, Long empresaId);

    // filtros nulos são ignorados
    @Query("""
            select v from Vantagem v
            where (:busca is null
                   or lower(v.nome) like lower(concat('%', :busca, '%'))
                   or lower(v.descricao) like lower(concat('%', :busca, '%')))
              and (:empresaId is null or v.empresa.id = :empresaId)
            """)
    List<Vantagem> buscarCatalogo(String busca, Long empresaId, Sort sort);
}
