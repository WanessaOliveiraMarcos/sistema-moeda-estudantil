package com.exemplo.app.service;

import com.exemplo.app.dto.AlunoForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.Curso;
import com.exemplo.app.model.Instituicao;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.CursoRepository;
import com.exemplo.app.repository.InstituicaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioService usuarioService;

    /** Alunos matriculados nas turmas do professor; turmaId nulo considera todas as turmas dele. */
    public List<Aluno> listarDoProfessor(Long professorId, Long turmaId, String busca) {
        return alunoRepository.buscarDoProfessor(professorId, turmaId, busca == null || busca.isBlank() ? null : busca.trim());
    }

    /** instituicaoId nulo lista alunos de todas as instituições. */
    public List<Aluno> listar(Long instituicaoId, String busca) {
        return alunoRepository.buscar(instituicaoId, busca == null || busca.isBlank() ? null : busca.trim());
    }

    public Aluno buscar(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Aluno não encontrado"));
    }

    @Transactional
    public Aluno salvar(AlunoForm form) {
        Aluno aluno = form.getId() == null ? new Aluno() : buscar(form.getId());

        String cpf = form.getCpf().replaceAll("\\D", "");
        boolean cpfEmUso = aluno.getId() == null
                ? alunoRepository.existsByCpf(cpf)
                : alunoRepository.existsByCpfAndIdNot(cpf, aluno.getId());
        if (cpfEmUso) {
            throw new RegraNegocioException("cpf", "CPF já cadastrado");
        }

        Instituicao instituicao = instituicaoRepository.findById(form.getInstituicaoId())
                .orElseThrow(() -> new RegraNegocioException("instituicaoId", "Instituição inválida"));
        Curso curso = cursoRepository.findById(form.getCursoId())
                .orElseThrow(() -> new RegraNegocioException("cursoId", "Curso inválido"));
        if (!curso.getInstituicao().getId().equals(instituicao.getId())) {
            throw new RegraNegocioException("cursoId", "O curso não pertence à instituição selecionada");
        }

        usuarioService.definirEmail(aluno, form.getEmail());
        usuarioService.definirSenha(aluno, form.getSenha());
        aluno.setNome(form.getNome().trim());
        aluno.setCpf(cpf);
        aluno.setRg(form.getRg().trim());
        aluno.setEndereco(form.getEndereco().trim());
        aluno.setInstituicao(instituicao);
        aluno.setCurso(curso);
        return alunoRepository.save(aluno);
    }

    @Transactional
    public void excluir(Long id) {
        alunoRepository.delete(buscar(id));
        alunoRepository.flush();
    }
}
