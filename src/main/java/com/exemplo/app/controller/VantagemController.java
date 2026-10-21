package com.exemplo.app.controller;

import com.exemplo.app.dto.VantagemForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.EmpresaParceiraService;
import com.exemplo.app.service.VantagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Vantagens da empresa logada (US08/US09) e quem as resgatou.
 * A empresa vem sempre do login, nunca da URL, então uma empresa não acessa as vantagens de outra.
 */
@Controller
@RequestMapping("/minhas-vantagens")
@RequiredArgsConstructor
public class VantagemController {

    private final VantagemService vantagemService;
    private final EmpresaParceiraService empresaService;

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioLogado empresa, Model model) {
        model.addAttribute("empresa", empresaService.buscar(empresa.getId()));
        model.addAttribute("vantagens", vantagemService.listarPorEmpresa(empresa.getId()));
        return "vantagens/lista";
    }

    @GetMapping("/resgates")
    public String resgates(@AuthenticationPrincipal UsuarioLogado empresa, Model model) {
        model.addAttribute("empresa", empresaService.buscar(empresa.getId()));
        model.addAttribute("resgates", vantagemService.listarResgates(empresa.getId()));
        return "vantagens/resgates";
    }

    @GetMapping("/novo")
    public String novo(@AuthenticationPrincipal UsuarioLogado empresa, Model model) {
        model.addAttribute("vantagem", new VantagemForm());
        return formulario(empresa, model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@AuthenticationPrincipal UsuarioLogado empresa, @PathVariable Long id, Model model) {
        model.addAttribute("vantagem", VantagemForm.de(vantagemService.buscar(empresa.getId(), id)));
        return formulario(empresa, model);
    }

    @PostMapping
    public String salvar(@AuthenticationPrincipal UsuarioLogado empresa,
                         @Valid @ModelAttribute("vantagem") VantagemForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return formulario(empresa, model);
        }
        try {
            vantagemService.salvar(empresa.getId(), form);
        } catch (RegraNegocioException e) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
            return formulario(empresa, model);
        }
        redirect.addFlashAttribute("mensagem", "Vantagem salva com sucesso");
        return "redirect:/minhas-vantagens";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@AuthenticationPrincipal UsuarioLogado empresa, @PathVariable Long id,
                          RedirectAttributes redirect) {
        try {
            vantagemService.excluir(empresa.getId(), id);
            redirect.addFlashAttribute("mensagem", "Vantagem excluída");
        } catch (DataIntegrityViolationException e) {
            redirect.addFlashAttribute("erro", "Não é possível excluir uma vantagem que já foi resgatada");
        }
        return "redirect:/minhas-vantagens";
    }

    private String formulario(UsuarioLogado empresa, Model model) {
        model.addAttribute("empresa", empresaService.buscar(empresa.getId()));
        return "vantagens/form";
    }
}
