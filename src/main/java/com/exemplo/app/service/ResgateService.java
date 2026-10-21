package com.exemplo.app.service;

import com.exemplo.app.email.Email;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.TrocaVantagem;
import com.exemplo.app.model.Vantagem;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.TrocaVantagemRepository;
import com.exemplo.app.repository.VantagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;

/**
 * Aluno troca moedas por uma vantagem (US17). O cupom vai por e-mail para o aluno (US18)
 * e a empresa recebe o mesmo código para conferir a troca (US19).
 */
@Service
@RequiredArgsConstructor
public class ResgateService {

    // sem 0/O e 1/I, que se confundem ao ler o código no balcão
    private static final String CARACTERES_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AlunoRepository alunoRepository;
    private final VantagemRepository vantagemRepository;
    private final TrocaVantagemRepository trocaRepository;
    private final ApplicationEventPublisher eventos;

    @Transactional
    public TrocaVantagem resgatar(Long alunoId, Long vantagemId) {
        Aluno aluno = alunoRepository.buscarParaAtualizar(alunoId).orElseThrow();
        Vantagem vantagem = vantagemRepository.findById(vantagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vantagem não encontrada"));
        if (aluno.getSaldoMoedas() < vantagem.getValor()) {
            throw new RegraNegocioException("vantagem", "Saldo insuficiente: \"" + vantagem.getNome() + "\" custa "
                    + vantagem.getValor() + " moedas e você tem " + aluno.getSaldoMoedas());
        }

        aluno.setSaldoMoedas(aluno.getSaldoMoedas() - vantagem.getValor());

        TrocaVantagem troca = new TrocaVantagem();
        troca.setAluno(aluno);
        troca.setVantagem(vantagem);
        troca.setValor(vantagem.getValor());
        troca.setCodigo(novoCodigo());
        trocaRepository.save(troca);

        String quando = troca.getDataHora().format(DATA);
        eventos.publishEvent(new Email(aluno.getEmail(),
                "Seu cupom: " + vantagem.getNome(),
                """
                Olá, %s!

                Você trocou %d moedas por "%s" (%s).

                CÓDIGO DO CUPOM: %s

                Apresente este código no atendimento da empresa para usar a vantagem.
                Data da troca: %s
                Saldo restante: %d moedas

                Sistema de Moeda Estudantil
                """.formatted(aluno.getNome(), troca.getValor(), vantagem.getNome(),
                        vantagem.getEmpresa().getNome(), troca.getCodigo(), quando, aluno.getSaldoMoedas())));
        eventos.publishEvent(new Email(vantagem.getEmpresa().getEmail(),
                "Novo resgate: " + vantagem.getNome() + " (" + troca.getCodigo() + ")",
                """
                Olá, %s!

                O aluno %s (%s) resgatou a vantagem "%s".

                CÓDIGO PARA CONFERÊNCIA: %s

                Confira este código quando o aluno apresentar o cupom no atendimento.
                Data da troca: %s

                Sistema de Moeda Estudantil
                """.formatted(vantagem.getEmpresa().getResponsavel(), aluno.getNome(), aluno.getEmail(),
                        vantagem.getNome(), troca.getCodigo(), quando)));
        return troca;
    }

    // formato MOE-XXXX-XXXX
    private String novoCodigo() {
        String codigo;
        do {
            StringBuilder sb = new StringBuilder("MOE-");
            for (int i = 0; i < 8; i++) {
                if (i == 4) {
                    sb.append('-');
                }
                sb.append(CARACTERES_CODIGO.charAt(ALEATORIO.nextInt(CARACTERES_CODIGO.length())));
            }
            codigo = sb.toString();
        } while (trocaRepository.existsByCodigo(codigo));
        return codigo;
    }
}
