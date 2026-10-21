package com.exemplo.app.controller;

import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.StatusTransferencia;
import com.exemplo.app.model.TransferenciaAluno;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.TransferenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Professor aprova ou recusa os envios de moedas entre alunos das suas turmas. */
@Controller
@RequestMapping("/autorizacoes")
@RequiredArgsConstructor
public class AutorizacaoController {

    private final TransferenciaService transferenciaService;

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioLogado professor, Model model) {
        model.addAttribute("pendentes", transferenciaService.pendentesDoProfessor(professor.getId()));
        model.addAttribute("decididas", transferenciaService.decididasDoProfessor(professor.getId()));
        return "autorizacoes";
    }

    @PostMapping("/{id}/aprovar")
    public String aprovar(@AuthenticationPrincipal UsuarioLogado professor, @PathVariable Long id,
                          RedirectAttributes redirect) {
        try {
            TransferenciaAluno t = transferenciaService.aprovar(professor.getId(), id);
            if (t.getStatus() == StatusTransferencia.APROVADA) {
                redirect.addFlashAttribute("mensagem", t.getValor() + " moedas transferidas de "
                        + t.getRemetente().getNome() + " para " + t.getDestinatario().getNome() + ".");
            } else {
                redirect.addFlashAttribute("erro", "Não foi possível aprovar: " + t.getMotivoRecusa().toLowerCase()
                        + ". O pedido foi recusado.");
            }
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/autorizacoes";
    }

    @PostMapping("/{id}/recusar")
    public String recusar(@AuthenticationPrincipal UsuarioLogado professor, @PathVariable Long id,
                          @RequestParam(required = false) String motivo, RedirectAttributes redirect) {
        try {
            TransferenciaAluno t = transferenciaService.recusar(professor.getId(), id, motivo);
            redirect.addFlashAttribute("mensagem", "Pedido de " + t.getRemetente().getNome() + " recusado.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/autorizacoes";
    }
}
