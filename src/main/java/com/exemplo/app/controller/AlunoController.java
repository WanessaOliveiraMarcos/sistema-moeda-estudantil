package com.exemplo.app.controller;

import com.exemplo.app.model.Instituicao;
import com.exemplo.app.model.Perfil;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.AlunoService;
import com.exemplo.app.service.InstituicaoService;
import com.exemplo.app.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Lista de alunos. Professor vê só os da própria instituição (e envia moedas a partir dela);
 * admin vê todos e pode filtrar por instituição. SecurityConfig libera só esses dois perfis.
 */
@Controller
@RequestMapping("/alunos")
@RequiredArgsConstructor
public class AlunoController {

    private final AlunoService alunoService;
    private final InstituicaoService instituicaoService;
    private final ProfessorService professorService;

    @GetMapping
    public String listar(@RequestParam(required = false) String busca,
                         @RequestParam(required = false) Long instituicaoId,
                         @AuthenticationPrincipal UsuarioLogado usuario, Model model) {
        if (usuario.getPerfil() == Perfil.PROFESSOR) {
            Instituicao instituicao = professorService.buscar(usuario.getId()).getDepartamento().getInstituicao();
            model.addAttribute("instituicao", instituicao);
            instituicaoId = instituicao.getId();
        } else {
            model.addAttribute("instituicoes", instituicaoService.listar());
            model.addAttribute("instituicaoId", instituicaoId);
        }
        model.addAttribute("alunos", alunoService.listar(instituicaoId, busca));
        model.addAttribute("busca", busca);
        return "alunos/lista";
    }
}
