package com.exemplo.app.controller;

import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.Perfil;
import com.exemplo.app.model.Turma;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.AlunoService;
import com.exemplo.app.service.InstituicaoService;
import com.exemplo.app.service.TurmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lista de alunos. Professor vê só os matriculados nas suas turmas (e envia moedas a partir dela);
 * admin vê todos e pode filtrar por instituição. SecurityConfig libera só esses dois perfis.
 */
@Controller
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;
    private final InstituicaoService instituicaoService;
    private final TurmaService turmaService;

    @GetMapping
    public String listar(@RequestParam(required = false) String busca,
                         @RequestParam(required = false) Long instituicaoId,
                         @RequestParam(required = false) Long turmaId,
                         @AuthenticationPrincipal UsuarioLogado usuario, Model model) {
        if (usuario.getPerfil() == Perfil.PROFESSOR) {
            List<Turma> turmas = turmaService.turmasDoProfessor(usuario.getId());
            model.addAttribute("turmas", turmas);
            model.addAttribute("turmaId", turmaId);
            // nomes das turmas (do professor) em que cada aluno está
            Map<Long, String> turmasDoAluno = new HashMap<>();
            for (Turma turma : turmas) {
                for (Aluno aluno : turma.getAlunos()) {
                    turmasDoAluno.merge(aluno.getId(), turma.getNome(), (a, b) -> a + ", " + b);
                }
            }
            model.addAttribute("turmasDoAluno", turmasDoAluno);
            model.addAttribute("alunos", alunoService.listarDoProfessor(usuario.getId(), turmaId, busca));
        } else {
            model.addAttribute("instituicoes", instituicaoService.listar());
            model.addAttribute("instituicaoId", instituicaoId);
            model.addAttribute("alunos", alunoService.listar(instituicaoId, busca));
        }
        model.addAttribute("busca", busca);
        return "alunos/lista";
    }
}
