package com.exemplo.app.config;

import com.exemplo.app.model.*;
import com.exemplo.app.repository.*;
import com.exemplo.app.service.ProfessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pré-cadastro de instituições, cursos, departamentos e professores
 * (enviados pela instituição no momento da parceria), só com o banco vazio.
 * Também garante um administrador, um aluno e uma empresa de demonstração para testar o login.
 */
@Component
@RequiredArgsConstructor
public class CargaInicial implements CommandLineRunner {

    private static final String SENHA_DEMONSTRACAO = "123456";

    private final InstituicaoRepository instituicaoRepository;
    private final CursoRepository cursoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final ProfessorRepository professorRepository;
    private final ProfessorService professorService;
    private final UsuarioRepository usuarioRepository;
    private final AlunoRepository alunoRepository;
    private final EmpresaParceiraRepository empresaRepository;
    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (instituicaoRepository.count() == 0) {
            carregarInstituicoesEProfessores();
        }
        carregarUsuariosDeDemonstracao();
    }

    private void carregarInstituicoesEProfessores() {
        Instituicao puc = instituicao("PUC Minas", "Av. Dom José Gaspar, 500 - Belo Horizonte/MG");
        Instituicao ufmg = instituicao("UFMG", "Av. Antônio Carlos, 6627 - Belo Horizonte/MG");

        curso("Engenharia de Software", 8, puc);
        curso("Ciência da Computação", 8, puc);
        curso("Sistemas de Informação", 8, ufmg);

        Departamento computacaoPuc = departamento("Instituto de Ciências Exatas e Informática", puc);
        Departamento computacaoUfmg = departamento("Departamento de Ciência da Computação", ufmg);

        professor("Ana Souza", "52998224725", "ana.souza@exemplo.com", computacaoPuc);
        professor("Carlos Lima", "11144477735", "carlos.lima@exemplo.com", computacaoUfmg);
    }

    // conferido pelo e-mail, então também é criado em bancos que já tinham a carga inicial
    private void carregarUsuariosDeDemonstracao() {
        if (!usuarioRepository.existsByEmail("admin@exemplo.com")) {
            Administrador admin = new Administrador();
            admin.setNome("Administrador");
            admin.setEmail("admin@exemplo.com");
            admin.setSenha(passwordEncoder.encode(SENHA_DEMONSTRACAO));
            administradorRepository.save(admin);
        }
        if (!usuarioRepository.existsByEmail("aluno@exemplo.com")) {
            cursoRepository.findFirstByNome("Engenharia de Software").ifPresent(curso -> {
                Aluno aluno = new Aluno();
                aluno.setNome("João Pereira");
                aluno.setEmail("aluno@exemplo.com");
                aluno.setSenha(passwordEncoder.encode(SENHA_DEMONSTRACAO));
                aluno.setCpf("93541134780");
                aluno.setRg("MG-12.345.678");
                aluno.setEndereco("Rua dos Estudantes, 100 - Belo Horizonte/MG");
                aluno.setInstituicao(curso.getInstituicao());
                aluno.setCurso(curso);
                alunoRepository.save(aluno);
            });
        }
        if (!usuarioRepository.existsByEmail("empresa@exemplo.com")) {
            EmpresaParceira empresa = new EmpresaParceira();
            empresa.setNome("Restaurante Universitário");
            empresa.setResponsavel("Paula Martins");
            empresa.setEmail("empresa@exemplo.com");
            empresa.setSenha(passwordEncoder.encode(SENHA_DEMONSTRACAO));
            empresa.setTelefone("(31) 3319-0000");
            empresa.setEndereco("Campus Coração Eucarístico - Belo Horizonte/MG");
            empresaRepository.save(empresa);
        }
    }

    private Instituicao instituicao(String nome, String endereco) {
        Instituicao instituicao = new Instituicao();
        instituicao.setNome(nome);
        instituicao.setEndereco(endereco);
        return instituicaoRepository.save(instituicao);
    }

    private void curso(String nome, int semestres, Instituicao instituicao) {
        Curso curso = new Curso();
        curso.setNome(nome);
        curso.setSemestres(semestres);
        curso.setInstituicao(instituicao);
        cursoRepository.save(curso);
    }

    private Departamento departamento(String nome, Instituicao instituicao) {
        Departamento departamento = new Departamento();
        departamento.setNome(nome);
        departamento.setInstituicao(instituicao);
        return departamentoRepository.save(departamento);
    }

    private void professor(String nome, String cpf, String email, Departamento departamento) {
        Professor professor = new Professor();
        professor.setNome(nome);
        professor.setCpf(cpf);
        professor.setEmail(email);
        professor.setSenha(passwordEncoder.encode(SENHA_DEMONSTRACAO));
        professor.setDepartamento(departamento);
        professorRepository.save(professor);
        professorService.saldoDoSemestre(professor);
    }
}
