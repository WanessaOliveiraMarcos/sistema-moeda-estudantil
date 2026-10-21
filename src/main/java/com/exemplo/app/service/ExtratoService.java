package com.exemplo.app.service;

import com.exemplo.app.dto.Movimentacao;
import com.exemplo.app.model.StatusTransferencia;
import com.exemplo.app.repository.TransacaoMoedasRepository;
import com.exemplo.app.repository.TransferenciaAlunoRepository;
import com.exemplo.app.repository.TrocaVantagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/** Extratos de aluno (US15) e professor (US14), mais recentes primeiro. */
@Service
@RequiredArgsConstructor
public class ExtratoService {

    private final TransacaoMoedasRepository transacaoRepository;
    private final TrocaVantagemRepository trocaRepository;
    private final TransferenciaAlunoRepository transferenciaRepository;

    // moedas recebidas de professores, envios aprovados entre colegas e trocas por vantagens
    public List<Movimentacao> doAluno(Long alunoId) {
        Stream<Movimentacao> recebidas = transacaoRepository.findByAlunoIdOrderByDataHoraDesc(alunoId).stream()
                .map(t -> new Movimentacao(t.getDataHora(), "Recebido",
                        "De " + t.getProfessor().getNome(), t.getMotivo(), t.getValor(), null));
        Stream<Movimentacao> trocas = trocaRepository.findByAlunoIdOrderByDataHoraDesc(alunoId).stream()
                .map(t -> new Movimentacao(t.getDataHora(), "Troca",
                        t.getVantagem().getNome(), t.getVantagem().getEmpresa().getNome(), -t.getValor(), t.getCodigo()));
        Stream<Movimentacao> deColegas = transferenciaRepository
                .findByDestinatarioIdAndStatus(alunoId, StatusTransferencia.APROVADA).stream()
                .map(t -> new Movimentacao(t.getDecididaEm(), "Recebido de colega",
                        "De " + t.getRemetente().getNome(), t.getCriterio().getDescricao() + " · " + t.getTurma().getNome(),
                        t.getValor(), null));
        Stream<Movimentacao> paraColegas = transferenciaRepository
                .findByRemetenteIdAndStatus(alunoId, StatusTransferencia.APROVADA).stream()
                .map(t -> new Movimentacao(t.getDecididaEm(), "Enviado a colega",
                        "Para " + t.getDestinatario().getNome(), t.getCriterio().getDescricao() + " · " + t.getTurma().getNome(),
                        -t.getValor(), null));
        return Stream.of(recebidas, deColegas, paraColegas, trocas).flatMap(s -> s)
                .sorted(Comparator.comparing(Movimentacao::dataHora).reversed())
                .toList();
    }

    // moedas enviadas aos alunos
    public List<Movimentacao> doProfessor(Long professorId) {
        return transacaoRepository.findByProfessorIdOrderByDataHoraDesc(professorId).stream()
                .map(t -> new Movimentacao(t.getDataHora(), "Enviado",
                        "Para " + t.getAluno().getNome(), t.getMotivo(), -t.getValor(), null))
                .toList();
    }
}
