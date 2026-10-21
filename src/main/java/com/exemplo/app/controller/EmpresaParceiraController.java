package com.exemplo.app.controller;

import com.exemplo.app.service.EmpresaParceiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Empresas parceiras cadastradas, para o admin visualizar. */
@Controller
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaParceiraController {

    private final EmpresaParceiraService empresaService;

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("empresas", empresaService.listar(busca));
        model.addAttribute("busca", busca);
        return "empresas/lista";
    }
}
