package com.exemplo.app.controller;

import com.exemplo.app.dto.InstituicaoForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Instituicao;
import com.exemplo.app.service.InstituicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/instituicoes")
@RequiredArgsConstructor
public class InstituicaoController {

    private final InstituicaoService instituicaoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("instituicoes", instituicaoService.listarResumos());
        return "instituicoes/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        InstituicaoForm form = new InstituicaoForm();
        form.getCursos().add(new InstituicaoForm.CursoLinha());
        form.getProfessores().add(new InstituicaoForm.ProfessorLinha());
        model.addAttribute("instituicao", form);
        return "instituicoes/form";
    }

    @PostMapping
    public String cadastrar(@Valid @ModelAttribute("instituicao") InstituicaoForm form, BindingResult result,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "instituicoes/form";
        }
        Instituicao instituicao;
        try {
            instituicao = instituicaoService.cadastrar(form);
        } catch (RegraNegocioException e) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
            return "instituicoes/form";
        }
        redirect.addFlashAttribute("mensagem", instituicao.getNome() + " cadastrada com "
                + form.getProfessores().size() + " professor(es).");
        return "redirect:/instituicoes";
    }
}
