package com.exemplo.app.controller;

import com.exemplo.app.dto.CriterioForm;
import com.exemplo.app.dto.TurmaForm;
import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.SaldoMoedasProfessor;
import com.exemplo.app.model.Turma;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.TurmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;

/** Turmas do professor logado e seus critérios de troca. */
@Controller
@RequestMapping("/turmas")
@RequiredArgsConstructor
public class TurmaController {

    private final TurmaService turmaService;

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioLogado professor, Model model) {
        TurmaForm form = new TurmaForm();
        form.setSemestre(SaldoMoedasProfessor.semestreAtual());
        model.addAttribute("turma", form);
        return lista(professor, model);
    }

    @PostMapping
    public String criar(@AuthenticationPrincipal UsuarioLogado professor,
                        @Valid @ModelAttribute("turma") TurmaForm form, BindingResult result,
                        Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return lista(professor, model);
        }
        Turma turma = turmaService.criar(professor.getId(), form);
        redirect.addFlashAttribute("mensagem", "Turma " + turma.getNome()
                + " criada. Cadastre os critérios de troca para os alunos poderem enviar moedas entre si.");
        return "redirect:/turmas/" + turma.getId();
    }

    @GetMapping("/{id}")
    public String detalhe(@AuthenticationPrincipal UsuarioLogado professor, @PathVariable Long id, Model model) {
        model.addAttribute("criterio", new CriterioForm());
        return paginaDetalhe(professor, id, model);
    }

    @PostMapping("/{id}/criterios")
    public String adicionarCriterio(@AuthenticationPrincipal UsuarioLogado professor, @PathVariable Long id,
                                    @Valid @ModelAttribute("criterio") CriterioForm form, BindingResult result,
                                    Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return paginaDetalhe(professor, id, model);
        }
        turmaService.adicionarCriterio(professor.getId(), id, form);
        redirect.addFlashAttribute("mensagem", "Critério \"" + form.getDescricao().trim() + "\" cadastrado.");
        return "redirect:/turmas/" + id;
    }

    @PostMapping("/{id}/criterios/{criterioId}/excluir")
    public String removerCriterio(@AuthenticationPrincipal UsuarioLogado professor, @PathVariable Long id,
                                  @PathVariable Long criterioId, RedirectAttributes redirect) {
        try {
            turmaService.removerCriterio(professor.getId(), id, criterioId);
            redirect.addFlashAttribute("mensagem", "Critério removido.");
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Este critério já foi usado em envios entre alunos e não pode ser removido.");
        }
        return "redirect:/turmas/" + id;
    }

    private String lista(UsuarioLogado professor, Model model) {
        model.addAttribute("turmas", turmaService.turmasDoProfessor(professor.getId()));
        model.addAttribute("limite", Turma.LIMITE_ALUNOS);
        return "turmas/lista";
    }

    private String paginaDetalhe(UsuarioLogado professor, Long id, Model model) {
        Turma turma = turmaService.buscarDoProfessor(professor.getId(), id);
        model.addAttribute("t", turma);
        model.addAttribute("alunos", turma.getAlunos().stream()
                .sorted(Comparator.comparing(Aluno::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList());
        model.addAttribute("limite", Turma.LIMITE_ALUNOS);
        return "turmas/detalhe";
    }
}
