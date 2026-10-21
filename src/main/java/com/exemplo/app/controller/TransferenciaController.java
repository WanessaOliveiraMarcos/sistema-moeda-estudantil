package com.exemplo.app.controller;

import com.exemplo.app.dto.TransferenciaForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Turma;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.TransferenciaService;
import com.exemplo.app.service.TurmaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Aluno pede para enviar moedas a um colega de turma. A página tem dois passos:
 * escolher a turma (GET ?turmaId=) e então o colega, o critério e o valor.
 */
@Controller
@RequestMapping("/transferencias")
@RequiredArgsConstructor
public class TransferenciaController {

    private final TransferenciaService transferenciaService;
    private final TurmaService turmaService;

    @GetMapping
    public String formulario(@RequestParam(required = false) Long turmaId,
                             @AuthenticationPrincipal UsuarioLogado aluno, Model model) {
        TransferenciaForm form = new TransferenciaForm();
        form.setTurmaId(turmaId);
        model.addAttribute("transferencia", form);
        return pagina(aluno, model);
    }

    @PostMapping
    public String solicitar(@Valid @ModelAttribute("transferencia") TransferenciaForm form, BindingResult result,
                            @AuthenticationPrincipal UsuarioLogado aluno, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return pagina(aluno, model);
        }
        try {
            var pedido = transferenciaService.solicitar(aluno.getId(), form);
            redirect.addFlashAttribute("mensagem", "Pedido de " + pedido.getValor() + " moedas para "
                    + pedido.getDestinatario().getNome() + " enviado. As moedas só saem do seu saldo depois da autorização de "
                    + pedido.getTurma().getProfessor().getNome() + ", responsável pela turma.");
        } catch (RegraNegocioException e) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
            return pagina(aluno, model);
        }
        return "redirect:/transferencias";
    }

    private String pagina(UsuarioLogado aluno, Model model) {
        List<Turma> turmas = turmaService.turmasDoAluno(aluno.getId());
        TransferenciaForm form = (TransferenciaForm) model.getAttribute("transferencia");
        // turma escolhida: colegas (todos menos o próprio aluno) e critérios dela
        turmas.stream()
                .filter(t -> form != null && t.getId().equals(form.getTurmaId()))
                .findFirst()
                .ifPresent(turma -> {
                    model.addAttribute("turmaEscolhida", turma);
                    model.addAttribute("colegas", turma.getAlunos().stream()
                            .filter(a -> !a.getId().equals(aluno.getId()))
                            .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                            .toList());
                });
        model.addAttribute("turmas", turmas);
        model.addAttribute("pedidos", transferenciaService.doAluno(aluno.getId()));
        return "transferencias/form";
    }
}
