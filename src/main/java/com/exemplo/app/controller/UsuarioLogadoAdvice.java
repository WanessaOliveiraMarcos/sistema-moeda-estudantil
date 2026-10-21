package com.exemplo.app.controller;

import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.AlunoService;
import com.exemplo.app.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Dados do usuário logado disponíveis em todas as páginas (menu, ações por perfil e saldo).
 * As views usam ${perfil} em th:if em vez de sec:authorize, então não dependem do
 * dialeto thymeleaf-extras-springsecurity. Quem protege as rotas continua sendo o SecurityConfig.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class UsuarioLogadoAdvice {

    private final AlunoService alunoService;
    private final ProfessorService professorService;

    @ModelAttribute("usuarioLogado")
    public UsuarioLogado usuarioLogado(@AuthenticationPrincipal UsuarioLogado usuario) {
        return usuario;
    }

    // "ALUNO", "PROFESSOR", "EMPRESA", "ADMIN" ou null para quem não está logado
    @ModelAttribute("perfil")
    public String perfil(@AuthenticationPrincipal UsuarioLogado usuario) {
        return usuario == null ? null : usuario.getPerfil().name();
    }

    @ModelAttribute("saldoAtual")
    public Integer saldoAtual(@AuthenticationPrincipal UsuarioLogado usuario) {
        if (usuario == null) {
            return null;
        }
        return switch (usuario.getPerfil()) {
            case ALUNO -> alunoService.buscar(usuario.getId()).getSaldoMoedas();
            case PROFESSOR -> professorService.saldoDoSemestre(professorService.buscar(usuario.getId())).getValorFinal();
            default -> null;
        };
    }
}
