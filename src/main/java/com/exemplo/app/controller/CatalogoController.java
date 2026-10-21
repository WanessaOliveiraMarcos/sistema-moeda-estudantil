package com.exemplo.app.controller;

import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.TrocaVantagem;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.EmpresaParceiraService;
import com.exemplo.app.service.ResgateService;
import com.exemplo.app.service.VantagemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Vitrine de vantagens de todas as empresas (US16) e resgate pelo aluno (US17). */
@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final VantagemService vantagemService;
    private final EmpresaParceiraService empresaService;
    private final ResgateService resgateService;

    @GetMapping("/vantagens")
    public String catalogo(@RequestParam(required = false) String busca,
                           @RequestParam(required = false) Long empresaId,
                           @RequestParam(defaultValue = "nome") String ordem,
                           Model model) {
        model.addAttribute("vantagens", vantagemService.catalogo(busca, empresaId, ordem));
        model.addAttribute("empresas", empresaService.listar());
        model.addAttribute("busca", busca);
        model.addAttribute("empresaId", empresaId);
        model.addAttribute("ordem", ordem);
        return "vantagens/catalogo";
    }

    @PostMapping("/vantagens/{id}/resgatar")
    public String resgatar(@PathVariable Long id, @AuthenticationPrincipal UsuarioLogado aluno,
                           RedirectAttributes redirect) {
        try {
            TrocaVantagem troca = resgateService.resgatar(aluno.getId(), id);
            redirect.addFlashAttribute("mensagem", "Resgate de \"" + troca.getVantagem().getNome()
                    + "\" realizado! Código do cupom: " + troca.getCodigo() + ". Enviamos o cupom para o seu e-mail.");
            return "redirect:/extrato";
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/vantagens";
        }
    }
}
