package com.exemplo.app.service;

import com.exemplo.app.dto.CriterioForm;
import com.exemplo.app.dto.TurmaForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.CriterioTroca;
import com.exemplo.app.model.Turma;
import com.exemplo.app.repository.CriterioTrocaRepository;
import com.exemplo.app.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Turmas do professor, matrícula dos alunos (até 40 por turma) e critérios de troca. */
@Service
@RequiredArgsConstructor
public class TurmaService {

    private final TurmaRepository turmaRepository;
    private final CriterioTrocaRepository criterioRepository;
    private final ProfessorService professorService;
    private final AlunoService alunoService;

    public List<Turma> turmasDoProfessor(Long professorId) {
        return turmaRepository.findByProfessorIdOrderBySemestreDescNomeAsc(professorId);
    }

    /** Turma do professor; outra turma qualquer dá "não encontrada". */
    public Turma buscarDoProfessor(Long professorId, Long turmaId) {
        return turmaRepository.findByIdAndProfessorId(turmaId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
    }

    public List<Turma> turmasDoAluno(Long alunoId) {
        return turmaRepository.buscarDoAluno(alunoId);
    }

    public List<Turma> turmasDisponiveis(Long alunoId) {
        Aluno aluno = alunoService.buscar(alunoId);
        return turmaRepository.buscarDisponiveis(aluno.getInstituicao().getId(), alunoId);
    }

    @Transactional
    public Turma criar(Long professorId, TurmaForm form) {
        Turma turma = new Turma();
        turma.setNome(form.getNome().trim());
        turma.setSemestre(form.getSemestre());
        turma.setProfessor(professorService.buscar(professorId));
        return turmaRepository.save(turma);
    }

    @Transactional
    public Turma matricular(Long alunoId, Long turmaId) {
        Aluno aluno = alunoService.buscar(alunoId);
        Turma turma = turmaRepository.buscarParaAtualizar(turmaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada"));
        if (!turma.getProfessor().getDepartamento().getInstituicao().getId().equals(aluno.getInstituicao().getId())) {
            throw new RegraNegocioException("turma", "Esta turma é de outra instituição");
        }
        if (turma.temAluno(alunoId)) {
            throw new RegraNegocioException("turma", "Você já está matriculado em " + turma.getNome());
        }
        if (turma.isLotada()) {
            throw new RegraNegocioException("turma", "A turma " + turma.getNome() + " já tem "
                    + Turma.LIMITE_ALUNOS + " alunos");
        }
        turma.getAlunos().add(aluno);
        return turma;
    }

    @Transactional
    public CriterioTroca adicionarCriterio(Long professorId, Long turmaId, CriterioForm form) {
        Turma turma = buscarDoProfessor(professorId, turmaId);
        CriterioTroca criterio = new CriterioTroca();
        criterio.setTurma(turma);
        criterio.setDescricao(form.getDescricao().trim());
        criterio.setValorMaximo(form.getValorMaximo());
        return criterioRepository.save(criterio);
    }

    @Transactional
    public void removerCriterio(Long professorId, Long turmaId, Long criterioId) {
        Turma turma = buscarDoProfessor(professorId, turmaId);
        CriterioTroca criterio = criterioRepository.findByIdAndTurmaId(criterioId, turma.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Critério não encontrado"));
        turma.getCriterios().remove(criterio);
        criterioRepository.delete(criterio);
        criterioRepository.flush();
    }
}
