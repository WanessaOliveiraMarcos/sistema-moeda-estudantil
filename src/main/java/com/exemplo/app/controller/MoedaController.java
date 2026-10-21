package com.exemplo.app.controller;

import com.exemplo.app.dto.EnvioMoedasForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.TransacaoMoedas;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.AlunoService;
import com.exemplo.app.service.MoedaService;
import com.exemplo.app.service.ProfessorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/moedas/enviar")
@RequiredArgsConstructor
public class MoedaController {

    private final MoedaService moedaService;
    private final ProfessorService professorService;
    private final AlunoService alunoService;

    @GetMapping
    public String formulario(@RequestParam(required = false) Long alunoId,
                             @AuthenticationPrincipal UsuarioLogado usuario, Model model) {
        EnvioMoedasForm form = new EnvioMoedasForm();
        form.setAlunoId(alunoId);
        model.addAttribute("envio", form);
        return formulario(usuario, model);
    }

    @PostMapping
    public String enviar(@Valid @ModelAttribute("envio") EnvioMoedasForm form, BindingResult result,
                         @AuthenticationPrincipal UsuarioLogado usuario, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(usuario, model);
        }
        TransacaoMoedas transacao;
        try {
            transacao = moedaService.enviar(usuario.getId(), form);
        } catch (RegraNegocioException e) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
            return formulario(usuario, model);
        }
        redirect.addFlashAttribute("mensagem", transacao.getValor() + " moedas enviadas para "
                + transacao.getAluno().getNome() + ". O aluno foi avisado por e-mail.");
        return "redirect:/extrato";
    }

    private String formulario(UsuarioLogado usuario, Model model) {
        Professor professor = professorService.buscar(usuario.getId());
        Long instituicaoId = professor.getDepartamento().getInstituicao().getId();
        model.addAttribute("alunos", alunoService.listar(instituicaoId, null));
        model.addAttribute("saldo", professorService.saldoDoSemestre(professor).getValorFinal());
        return "moedas/enviar";
    }
}
