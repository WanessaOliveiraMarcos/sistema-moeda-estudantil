package com.exemplo.app.controller;

import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Turma;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.TurmaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Turmas do aluno logado e matrícula nas turmas da sua instituição. */
@Controller
@RequestMapping("/minhas-turmas")
@RequiredArgsConstructor
public class MatriculaController {

    private final TurmaService turmaService;

    @GetMapping
    public String turmas(@AuthenticationPrincipal UsuarioLogado aluno, Model model) {
        model.addAttribute("minhas", turmaService.turmasDoAluno(aluno.getId()));
        model.addAttribute("disponiveis", turmaService.turmasDisponiveis(aluno.getId()));
        model.addAttribute("limite", Turma.LIMITE_ALUNOS);
        return "turmas/aluno";
    }

    @PostMapping("/{id}/matricular")
    public String matricular(@AuthenticationPrincipal UsuarioLogado aluno, @PathVariable Long id,
                             RedirectAttributes redirect) {
        try {
            Turma turma = turmaService.matricular(aluno.getId(), id);
            redirect.addFlashAttribute("mensagem", "Matrícula feita em " + turma.getNome() + ".");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/minhas-turmas";
    }
}
