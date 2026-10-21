package com.exemplo.app.service;

import com.exemplo.app.dto.InstituicaoForm;
import com.exemplo.app.dto.InstituicaoResumo;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Curso;
import com.exemplo.app.model.Departamento;
import com.exemplo.app.model.Instituicao;
import com.exemplo.app.model.Professor;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.CursoRepository;
import com.exemplo.app.repository.DepartamentoRepository;
import com.exemplo.app.repository.InstituicaoRepository;
import com.exemplo.app.repository.ProfessorRepository;
import com.exemplo.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Instituições parceiras: o admin cadastra cada uma já com seus cursos e professores. */
@Service
@RequiredArgsConstructor
public class InstituicaoService {

    private final InstituicaoRepository instituicaoRepository;
    private final CursoRepository cursoRepository;
    private final DepartamentoRepository departamentoRepository;
    private final AlunoRepository alunoRepository;
    private final ProfessorRepository professorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfessorService professorService;
    private final PasswordEncoder passwordEncoder;

    public List<Instituicao> listar() {
        return instituicaoRepository.findAll(Sort.by("nome"));
    }

    public List<Curso> listarCursos() {
        return cursoRepository.findAll(Sort.by("instituicao.nome", "nome"));
    }

    public List<InstituicaoResumo> listarResumos() {
        Map<Long, List<Curso>> cursos = cursoRepository.findAll(Sort.by("nome")).stream()
                .collect(Collectors.groupingBy(c -> c.getInstituicao().getId()));
        Map<Long, List<Departamento>> departamentos = departamentoRepository.findAll(Sort.by("nome")).stream()
                .collect(Collectors.groupingBy(d -> d.getInstituicao().getId()));
        Map<Long, List<Professor>> professores = professorRepository.findAll(Sort.by("nome")).stream()
                .collect(Collectors.groupingBy(p -> p.getDepartamento().getInstituicao().getId()));
        return listar().stream()
                .map(i -> new InstituicaoResumo(i,
                        cursos.getOrDefault(i.getId(), List.of()),
                        departamentos.getOrDefault(i.getId(), List.of()),
                        professores.getOrDefault(i.getId(), List.of()),
                        alunoRepository.countByInstituicaoId(i.getId())))
                .toList();
    }

    /**
     * Cria a instituição com seus cursos, departamentos e professores (a lista que a instituição
     * envia no momento da parceria). Cada professor já recebe as moedas do semestre atual.
     */
    @Transactional
    public Instituicao cadastrar(InstituicaoForm form) {
        validarProfessores(form.getProfessores());

        Instituicao instituicao = new Instituicao();
        instituicao.setNome(form.getNome().trim());
        instituicao.setEndereco(form.getEndereco().trim());
        instituicaoRepository.save(instituicao);

        for (InstituicaoForm.CursoLinha linha : form.getCursos()) {
            Curso curso = new Curso();
            curso.setNome(linha.getNome().trim());
            curso.setSemestres(linha.getSemestres());
            curso.setInstituicao(instituicao);
            cursoRepository.save(curso);
        }

        Map<String, Departamento> departamentos = new HashMap<>();
        for (InstituicaoForm.ProfessorLinha linha : form.getProfessores()) {
            String nomeDepartamento = linha.getDepartamento().trim();
            Departamento departamento = departamentos.computeIfAbsent(nomeDepartamento.toLowerCase(), chave -> {
                Departamento novo = new Departamento();
                novo.setNome(nomeDepartamento);
                novo.setInstituicao(instituicao);
                return departamentoRepository.save(novo);
            });

            Professor professor = new Professor();
            professor.setNome(linha.getNome().trim());
            professor.setCpf(somenteDigitos(linha.getCpf()));
            professor.setEmail(linha.getEmail().trim().toLowerCase());
            professor.setSenha(passwordEncoder.encode(linha.getSenha()));
            professor.setDepartamento(departamento);
            professorRepository.save(professor);
            professorService.saldoDoSemestre(professor);
        }
        return instituicao;
    }

    // e-mail e CPF únicos no sistema e sem repetição dentro da própria lista
    private void validarProfessores(List<InstituicaoForm.ProfessorLinha> professores) {
        Set<String> emails = new HashSet<>();
        Set<String> cpfs = new HashSet<>();
        for (int i = 0; i < professores.size(); i++) {
            String campo = "professores[" + i + "].";
            String email = professores.get(i).getEmail().trim().toLowerCase();
            String cpf = somenteDigitos(professores.get(i).getCpf());
            if (!emails.add(email)) {
                throw new RegraNegocioException(campo + "email", "E-mail repetido na lista");
            }
            if (usuarioRepository.existsByEmail(email)) {
                throw new RegraNegocioException(campo + "email", "E-mail já cadastrado");
            }
            if (!cpfs.add(cpf)) {
                throw new RegraNegocioException(campo + "cpf", "CPF repetido na lista");
            }
            if (professorRepository.existsByCpf(cpf)) {
                throw new RegraNegocioException(campo + "cpf", "CPF já cadastrado");
            }
        }
    }

    private static String somenteDigitos(String cpf) {
        return cpf.replaceAll("\\D", "");
    }
}
