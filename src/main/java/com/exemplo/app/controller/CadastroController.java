package com.exemplo.app.controller;

import com.exemplo.app.dto.AlunoForm;
import com.exemplo.app.dto.EmpresaParceiraForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.service.AlunoService;
import com.exemplo.app.service.EmpresaParceiraService;
import com.exemplo.app.service.InstituicaoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Auto cadastro sem login (US03 e US06). Só cria contas: o id enviado é ignorado,
 * então não dá para alterar cadastros existentes por aqui.
 */
@Controller
@RequestMapping("/cadastro")
@RequiredArgsConstructor
public class CadastroController {

    private final AlunoService alunoService;
    private final EmpresaParceiraService empresaService;
    private final InstituicaoService instituicaoService;

    @GetMapping("/aluno")
    public String novoAluno(Model model) {
        model.addAttribute("aluno", new AlunoForm());
        return formularioAluno(model);
    }

    @PostMapping("/aluno")
    public String cadastrarAluno(@Valid @ModelAttribute("aluno") AlunoForm form, BindingResult result, Model model,
                                 HttpServletRequest request, HttpServletResponse response) {
        form.setId(null);
        if (result.hasErrors()) {
            return formularioAluno(model);
        }
        try {
            alunoService.salvar(form);
        } catch (RegraNegocioException e) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
            return formularioAluno(model);
        }
        return irParaLogin(request, response);
    }

    @GetMapping("/empresa")
    public String novaEmpresa(Model model) {
        model.addAttribute("empresa", new EmpresaParceiraForm());
        return formularioEmpresa(model);
    }

    @PostMapping("/empresa")
    public String cadastrarEmpresa(@Valid @ModelAttribute("empresa") EmpresaParceiraForm form, BindingResult result,
                                   Model model, HttpServletRequest request, HttpServletResponse response) {
        form.setId(null);
        if (result.hasErrors()) {
            return formularioEmpresa(model);
        }
        try {
            empresaService.salvar(form);
        } catch (RegraNegocioException e) {
            result.rejectValue(e.getCampo(), "regra", e.getMessage());
            return formularioEmpresa(model);
        }
        return irParaLogin(request, response);
    }

    // encerra a sessão de quem estiver logado neste navegador; sem isso o /login redirecionaria
    // para a área desse usuário e a pessoa não veria a tela de login com "Cadastro realizado"
    private String irParaLogin(HttpServletRequest request, HttpServletResponse response) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, autenticacao);
        return "redirect:/login?cadastrado";
    }

    private String formularioAluno(Model model) {
        model.addAttribute("instituicoes", instituicaoService.listar());
        model.addAttribute("cursos", instituicaoService.listarCursos());
        return "alunos/form";
    }

    private String formularioEmpresa(Model model) {
        return "empresas/form";
    }
}
