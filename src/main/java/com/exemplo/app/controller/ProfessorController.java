package com.exemplo.app.controller;

import com.exemplo.app.model.SaldoMoedasProfessor;
import com.exemplo.app.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    @GetMapping("/professores")
    public String listar(Model model) {
        String semestre = SaldoMoedasProfessor.semestreAtual();
        model.addAttribute("semestre", semestre);
        model.addAttribute("professores", professorService.listarComSaldo());
        return "professores/lista";
    }
}
