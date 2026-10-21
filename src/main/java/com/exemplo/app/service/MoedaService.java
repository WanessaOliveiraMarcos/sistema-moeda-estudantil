package com.exemplo.app.service;

import com.exemplo.app.dto.EnvioMoedasForm;
import com.exemplo.app.email.Email;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.SaldoMoedasProfessor;
import com.exemplo.app.model.TransacaoMoedas;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.SaldoMoedasProfessorRepository;
import com.exemplo.app.repository.TransacaoMoedasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Professor envia moedas a um aluno da sua instituição (US11) e o aluno é avisado por e-mail (US12). */
@Service
@RequiredArgsConstructor
public class MoedaService {

    private final AlunoRepository alunoRepository;
    private final SaldoMoedasProfessorRepository saldoRepository;
    private final TransacaoMoedasRepository transacaoRepository;
    private final ProfessorService professorService;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public TransacaoMoedas enviar(Long professorId, EnvioMoedasForm form) {
        Professor professor = professorService.buscar(professorId);
        Aluno aluno = alunoRepository.buscarParaAtualizar(form.getAlunoId())
                .orElseThrow(() -> new RegraNegocioException("alunoId", "Aluno não encontrado"));
        if (!aluno.getInstituicao().getId().equals(professor.getDepartamento().getInstituicao().getId())) {
            throw new RegraNegocioException("alunoId", "Você só pode enviar moedas a alunos da sua instituição");
        }

        professorService.saldoDoSemestre(professor);
        SaldoMoedasProfessor saldo = saldoRepository
                .buscarParaAtualizar(professorId, SaldoMoedasProfessor.semestreAtual())
                .orElseThrow();
        if (saldo.getValorFinal() < form.getValor()) {
            throw new RegraNegocioException("valor", "Saldo insuficiente: você tem " + saldo.getValorFinal() + " moedas");
        }

        saldo.setValorFinal(saldo.getValorFinal() - form.getValor());
        aluno.setSaldoMoedas(aluno.getSaldoMoedas() + form.getValor());

        TransacaoMoedas transacao = new TransacaoMoedas();
        transacao.setProfessor(professor);
        transacao.setAluno(aluno);
        transacao.setValor(form.getValor());
        transacao.setMotivo(form.getMotivo().trim());
        transacaoRepository.save(transacao);

        eventos.publishEvent(new Email(aluno.getEmail(),
                "Você recebeu " + transacao.getValor() + " moedas!",
                """
                Olá, %s!

                O professor %s enviou %d moedas para você.

                Motivo: %s

                Seu saldo agora é de %d moedas. Use-as para trocar por vantagens no sistema.

                Sistema de Moeda Estudantil
                """.formatted(aluno.getNome(), professor.getNome(), transacao.getValor(),
                        transacao.getMotivo(), aluno.getSaldoMoedas())));
        return transacao;
    }
}
