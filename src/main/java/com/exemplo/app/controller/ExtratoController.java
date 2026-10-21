package com.exemplo.app.controller;

import com.exemplo.app.model.Perfil;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.SaldoMoedasProfessor;
import com.exemplo.app.security.UsuarioLogado;
import com.exemplo.app.service.AlunoService;
import com.exemplo.app.service.ExtratoService;
import com.exemplo.app.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/** Saldo e movimentações do aluno ou do professor logado. */
@Controller
@RequiredArgsConstructor
public class ExtratoController {

    private final ExtratoService extratoService;
    private final AlunoService alunoService;
    private final ProfessorService professorService;

    @GetMapping("/extrato")
    public String extrato(@AuthenticationPrincipal UsuarioLogado usuario, Model model) {
        if (usuario.getPerfil() == Perfil.ALUNO) {
            model.addAttribute("saldo", alunoService.buscar(usuario.getId()).getSaldoMoedas());
            model.addAttribute("movimentacoes", extratoService.doAluno(usuario.getId()));
        } else {
            Professor professor = professorService.buscar(usuario.getId());
            SaldoMoedasProfessor saldoSemestre = professorService.saldoDoSemestre(professor);
            model.addAttribute("saldoSemestre", saldoSemestre);
            model.addAttribute("saldo", saldoSemestre.getValorFinal());
            model.addAttribute("movimentacoes", extratoService.doProfessor(usuario.getId()));
        }
        return "extrato";
    }
}
