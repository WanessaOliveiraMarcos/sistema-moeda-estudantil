package com.exemplo.app.service;

import com.exemplo.app.dto.ProfessorResumo;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.SaldoMoedasProfessor;
import com.exemplo.app.repository.ProfessorRepository;
import com.exemplo.app.repository.SaldoMoedasProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final SaldoMoedasProfessorRepository saldoRepository;

    public Professor buscar(Long id) {
        return professorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professor não encontrado"));
    }

    /**
     * Saldo do professor no semestre atual (US10). Na primeira consulta do semestre, credita
     * 1.000 moedas somadas ao que sobrou do último semestre em que ele teve saldo.
     */
    @Transactional
    public SaldoMoedasProfessor saldoDoSemestre(Professor professor) {
        String semestre = SaldoMoedasProfessor.semestreAtual();
        return saldoRepository.findByProfessorIdAndSemestre(professor.getId(), semestre)
                .orElseGet(() -> {
                    int sobra = saldoRepository.findTopByProfessorIdOrderBySemestreDesc(professor.getId())
                            .map(SaldoMoedasProfessor::getValorFinal)
                            .orElse(0);
                    SaldoMoedasProfessor saldo = new SaldoMoedasProfessor();
                    saldo.setProfessor(professor);
                    saldo.setSemestre(semestre);
                    saldo.setValorInicial(sobra + SaldoMoedasProfessor.MOEDAS_POR_SEMESTRE);
                    saldo.setValorFinal(saldo.getValorInicial());
                    return saldoRepository.save(saldo);
                });
    }

    @Transactional
    public List<ProfessorResumo> listarComSaldo() {
        return professorRepository.findAll(Sort.by("nome")).stream()
                .map(p -> new ProfessorResumo(p, saldoDoSemestre(p).getValorFinal()))
                .toList();
    }
}
