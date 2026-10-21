package com.exemplo.app.controller;

import com.exemplo.app.security.UsuarioLogado;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // cada perfil tem sua própria área inicial (US01)
    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioLogado usuario) {
        return switch (usuario.getPerfil()) {
            case ALUNO -> "redirect:/vantagens";
            case PROFESSOR -> "redirect:/alunos";
            case EMPRESA -> "redirect:/minhas-vantagens";
            case ADMIN -> "redirect:/instituicoes";
        };
    }
}
