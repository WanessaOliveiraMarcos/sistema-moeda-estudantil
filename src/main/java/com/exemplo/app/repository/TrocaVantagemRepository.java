package com.exemplo.app.repository;

import com.exemplo.app.model.TrocaVantagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrocaVantagemRepository extends JpaRepository<TrocaVantagem, Long> {

    List<TrocaVantagem> findByVantagemEmpresaIdOrderByDataHoraDesc(Long empresaId);

    List<TrocaVantagem> findByAlunoIdOrderByDataHoraDesc(Long alunoId);

    boolean existsByCodigo(String codigo);
}
