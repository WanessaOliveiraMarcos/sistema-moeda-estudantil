package com.exemplo.app.service;

import com.exemplo.app.dto.TransferenciaForm;
import com.exemplo.app.email.Email;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.CriterioTroca;
import com.exemplo.app.model.StatusTransferencia;
import com.exemplo.app.model.TransferenciaAluno;
import com.exemplo.app.model.Turma;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.CriterioTrocaRepository;
import com.exemplo.app.repository.TransferenciaAlunoRepository;
import com.exemplo.app.repository.TurmaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Envio de moedas entre alunos da mesma turma. O aluno faz o pedido seguindo um critério
 * da turma; as moedas só mudam de dono quando o professor da turma aprova.
 */
@Service
@RequiredArgsConstructor
public class TransferenciaService {

    private final TransferenciaAlunoRepository transferenciaRepository;
    private final TurmaRepository turmaRepository;
    private final CriterioTrocaRepository criterioRepository;
    private final AlunoRepository alunoRepository;
    private final AlunoService alunoService;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public TransferenciaAluno solicitar(Long remetenteId, TransferenciaForm form) {
        Aluno remetente = alunoService.buscar(remetenteId);
        Turma turma = turmaRepository.findById(form.getTurmaId())
                .filter(t -> t.temAluno(remetenteId))
                .orElseThrow(() -> new RegraNegocioException("turmaId", "Você não está matriculado nesta turma"));
        if (form.getDestinatarioId().equals(remetenteId)) {
            throw new RegraNegocioException("destinatarioId", "Escolha um colega, não você mesmo");
        }
        if (!turma.temAluno(form.getDestinatarioId())) {
            throw new RegraNegocioException("destinatarioId", "O colega precisa estar na mesma turma");
        }
        CriterioTroca criterio = criterioRepository.findByIdAndTurmaId(form.getCriterioId(), turma.getId())
                .orElseThrow(() -> new RegraNegocioException("criterioId", "Critério inválido para esta turma"));
        if (form.getValor() > criterio.getValorMaximo()) {
            throw new RegraNegocioException("valor", "O critério \"" + criterio.getDescricao() + "\" permite até "
                    + criterio.getValorMaximo() + " moedas");
        }
        if (form.getValor() > remetente.getSaldoMoedas()) {
            throw new RegraNegocioException("valor", "Saldo insuficiente: você tem " + remetente.getSaldoMoedas() + " moedas");
        }

        TransferenciaAluno transferencia = new TransferenciaAluno();
        transferencia.setTurma(turma);
        transferencia.setRemetente(remetente);
        transferencia.setDestinatario(alunoService.buscar(form.getDestinatarioId()));
        transferencia.setCriterio(criterio);
        transferencia.setValor(form.getValor());
        transferencia.setMensagem(form.getMensagem() == null || form.getMensagem().isBlank() ? null : form.getMensagem().trim());
        transferenciaRepository.save(transferencia);

        eventos.publishEvent(new Email(turma.getProfessor().getEmail(),
                "Autorização pendente: " + remetente.getNome() + " → " + transferencia.getDestinatario().getNome(),
                """
                Olá, %s!

                %s pediu para enviar %d moedas a %s na turma %s.
                Critério: %s

                Acesse "Autorizações" no sistema para aprovar ou recusar.

                Sistema de Moeda Estudantil
                """.formatted(turma.getProfessor().getNome(), remetente.getNome(), transferencia.getValor(),
                        transferencia.getDestinatario().getNome(), turma.getNome(), criterio.getDescricao())));
        return transferencia;
    }

    public List<TransferenciaAluno> doAluno(Long alunoId) {
        return transferenciaRepository.buscarDoAluno(alunoId);
    }

    public List<TransferenciaAluno> pendentesDoProfessor(Long professorId) {
        return transferenciaRepository.findByTurmaProfessorIdAndStatusOrderByDataHoraAsc(professorId,
                StatusTransferencia.PENDENTE);
    }

    public List<TransferenciaAluno> decididasDoProfessor(Long professorId) {
        return transferenciaRepository.findTop20ByTurmaProfessorIdAndStatusNotOrderByDecididaEmDesc(professorId,
                StatusTransferencia.PENDENTE);
    }

    public long contarPendentes(Long professorId) {
        return transferenciaRepository.countByTurmaProfessorIdAndStatus(professorId, StatusTransferencia.PENDENTE);
    }

    /**
     * Aprova e transfere. Se o remetente não tiver mais saldo (gastou depois do pedido),
     * o pedido é recusado automaticamente e o método devolve a transferência recusada.
     */
    @Transactional
    public TransferenciaAluno aprovar(Long professorId, Long transferenciaId) {
        TransferenciaAluno transferencia = pendenteDoProfessor(professorId, transferenciaId);

        // trava os dois saldos, sempre na mesma ordem (menor id primeiro) para evitar deadlock
        Long remetenteId = transferencia.getRemetente().getId();
        Long destinatarioId = transferencia.getDestinatario().getId();
        Aluno primeiro = alunoRepository.buscarParaAtualizar(Math.min(remetenteId, destinatarioId)).orElseThrow();
        Aluno segundo = alunoRepository.buscarParaAtualizar(Math.max(remetenteId, destinatarioId)).orElseThrow();
        Aluno remetente = primeiro.getId().equals(remetenteId) ? primeiro : segundo;
        Aluno destinatario = primeiro.getId().equals(destinatarioId) ? primeiro : segundo;

        if (remetente.getSaldoMoedas() < transferencia.getValor()) {
            return recusarPedido(transferencia, "Saldo insuficiente do remetente no momento da autorização");
        }
        remetente.setSaldoMoedas(remetente.getSaldoMoedas() - transferencia.getValor());
        destinatario.setSaldoMoedas(destinatario.getSaldoMoedas() + transferencia.getValor());
        transferencia.setStatus(StatusTransferencia.APROVADA);
        transferencia.setDecididaEm(LocalDateTime.now());

        String detalhe = "Turma: %s\nCritério: %s%s".formatted(transferencia.getTurma().getNome(),
                transferencia.getCriterio().getDescricao(),
                transferencia.getMensagem() == null ? "" : "\nMensagem: " + transferencia.getMensagem());
        eventos.publishEvent(new Email(destinatario.getEmail(),
                "Você recebeu " + transferencia.getValor() + " moedas de " + remetente.getNome(),
                """
                Olá, %s!

                %s enviou %d moedas para você, com autorização do professor.
                %s

                Seu saldo agora é de %d moedas.

                Sistema de Moeda Estudantil
                """.formatted(destinatario.getNome(), remetente.getNome(), transferencia.getValor(), detalhe,
                        destinatario.getSaldoMoedas())));
        eventos.publishEvent(new Email(remetente.getEmail(),
                "Envio aprovado: " + transferencia.getValor() + " moedas para " + destinatario.getNome(),
                """
                Olá, %s!

                O professor aprovou o envio de %d moedas para %s.
                %s

                Seu saldo agora é de %d moedas.

                Sistema de Moeda Estudantil
                """.formatted(remetente.getNome(), transferencia.getValor(), destinatario.getNome(), detalhe,
                        remetente.getSaldoMoedas())));
        return transferencia;
    }

    @Transactional
    public TransferenciaAluno recusar(Long professorId, Long transferenciaId, String motivo) {
        return recusarPedido(pendenteDoProfessor(professorId, transferenciaId),
                motivo == null || motivo.isBlank() ? null : motivo.trim());
    }

    private TransferenciaAluno recusarPedido(TransferenciaAluno transferencia, String motivo) {
        transferencia.setStatus(StatusTransferencia.RECUSADA);
        transferencia.setDecididaEm(LocalDateTime.now());
        transferencia.setMotivoRecusa(motivo);
        eventos.publishEvent(new Email(transferencia.getRemetente().getEmail(),
                "Envio recusado: " + transferencia.getValor() + " moedas para " + transferencia.getDestinatario().getNome(),
                """
                Olá, %s!

                O envio de %d moedas para %s (turma %s) não foi autorizado.%s

                Nenhuma moeda foi descontada.

                Sistema de Moeda Estudantil
                """.formatted(transferencia.getRemetente().getNome(), transferencia.getValor(),
                        transferencia.getDestinatario().getNome(), transferencia.getTurma().getNome(),
                        motivo == null ? "" : "\nMotivo: " + motivo)));
        return transferencia;
    }

    // só o professor da turma decide, e só pedidos ainda pendentes
    private TransferenciaAluno pendenteDoProfessor(Long professorId, Long transferenciaId) {
        TransferenciaAluno transferencia = transferenciaRepository.buscarParaAtualizar(transferenciaId)
                .filter(t -> t.getTurma().getProfessor().getId().equals(professorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
        if (transferencia.getStatus() != StatusTransferencia.PENDENTE) {
            throw new RegraNegocioException("status", "Este pedido já foi " + transferencia.getStatus().getDescricao().toLowerCase());
        }
        return transferencia;
    }
}
