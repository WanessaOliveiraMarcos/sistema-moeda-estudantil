package com.exemplo.app.repository;

import com.exemplo.app.model.EmpresaParceira;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpresaParceiraRepository extends JpaRepository<EmpresaParceira, Long> {

    List<EmpresaParceira> findAllByOrderByNomeAsc();

    List<EmpresaParceira> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
