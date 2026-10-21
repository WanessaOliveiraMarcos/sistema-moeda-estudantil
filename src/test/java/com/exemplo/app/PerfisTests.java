package com.exemplo.app;

import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.Curso;
import com.exemplo.app.model.EmpresaParceira;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.TrocaVantagem;
import com.exemplo.app.model.Turma;
import com.exemplo.app.model.Vantagem;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.CursoRepository;
import com.exemplo.app.repository.EmpresaParceiraRepository;
import com.exemplo.app.repository.TrocaVantagemRepository;
import com.exemplo.app.repository.TurmaRepository;
import com.exemplo.app.repository.UsuarioRepository;
import com.exemplo.app.repository.VantagemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** O que cada perfil pode ver (US02). Usa os usuários da CargaInicial. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class PerfisTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private EmpresaParceiraRepository empresaRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private TrocaVantagemRepository trocaRepository;
    @Autowired
    private TurmaRepository turmaRepository;

    // aluna da UFMG numa turma do professor Carlos (o aluno de demonstração está na turma da Ana, na PUC Minas)
    // e um resgate na empresa de demonstração
    @BeforeEach
    void criarDados() {
        if (usuarioRepository.existsByEmail("aluna.ufmg@exemplo.com")) {
            return;
        }
        Curso sistemas = cursoRepository.findFirstByNome("Sistemas de Informação").orElseThrow();
        Aluno alunaUfmg = new Aluno();
        alunaUfmg.setNome("Beatriz UFMG");
        alunaUfmg.setEmail("aluna.ufmg@exemplo.com");
        alunaUfmg.setSenha("x");
        alunaUfmg.setCpf("39053344705");
        alunaUfmg.setRg("MG-1");
        alunaUfmg.setEndereco("Rua");
        alunaUfmg.setInstituicao(sistemas.getInstituicao());
        alunaUfmg.setCurso(sistemas);
        alunoRepository.save(alunaUfmg);

        Turma turmaUfmg = new Turma();
        turmaUfmg.setNome("Algoritmos");
        turmaUfmg.setSemestre("2026/2");
        turmaUfmg.setProfessor((Professor) usuarioRepository.findByEmail("carlos.lima@exemplo.com").orElseThrow());
        turmaUfmg.getAlunos().add(alunaUfmg);
        turmaRepository.save(turmaUfmg);

        EmpresaParceira empresa = (EmpresaParceira) usuarioRepository.findByEmail("empresa@exemplo.com").orElseThrow();
        Vantagem almoco = new Vantagem();
        almoco.setNome("Almoço grátis");
        almoco.setDescricao("Um almoço no RU");
        almoco.setFoto("vantagens/teste.png");
        almoco.setValor(40);
        almoco.setEmpresa(empresa);
        vantagemRepository.save(almoco);

        TrocaVantagem troca = new TrocaVantagem();
        troca.setAluno(alunaUfmg);
        troca.setVantagem(almoco);
        troca.setValor(40);
        troca.setCodigo("TESTE-123");
        trocaRepository.save(troca);
    }

    @Test
    @WithUserDetails("aluno@exemplo.com")
    void alunoSoVeAsVantagens() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/vantagens"));
        mvc.perform(get("/vantagens"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Almoço grátis")))
                .andExpect(content().string(containsString("40 moedas")));
        for (String url : new String[]{"/alunos", "/minhas-vantagens", "/minhas-vantagens/resgates",
                "/empresas", "/professores", "/instituicoes", "/alunos/novo", "/painel"}) {
            mvc.perform(get(url)).andExpect(status().isForbidden());
        }
    }

    @Test
    @WithUserDetails("aluno@exemplo.com")
    void menuDoAlunoTemSoAsOpcoesDele() throws Exception {
        mvc.perform(get("/vantagens"))
                // atributos sec: crus indicam que o dialeto de segurança do Thymeleaf não foi carregado
                .andExpect(content().string(not(containsString("sec:"))))
                .andExpect(content().string(containsString("href=\"/extrato\"")))
                .andExpect(content().string(not(containsString("href=\"/painel\""))))
                .andExpect(content().string(not(containsString("href=\"/moedas/enviar\""))))
                .andExpect(content().string(not(containsString("href=\"/minhas-vantagens\""))))
                .andExpect(content().string(not(containsString(">Entrar</a>"))))
                .andExpect(content().string(containsString("João Pereira")));
    }

    @Test
    @WithUserDetails("ana.souza@exemplo.com")
    void professorSoVeAlunosDasSuasTurmas() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/alunos"));
        mvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("João Pereira")))
                .andExpect(content().string(not(containsString("Beatriz UFMG"))));
        // a busca também fica limitada à instituição
        mvc.perform(get("/alunos").param("busca", "Beatriz"))
                .andExpect(content().string(not(containsString("aluna.ufmg@exemplo.com"))));
        for (String url : new String[]{"/vantagens", "/minhas-vantagens", "/alunos/novo", "/professores", "/painel"}) {
            mvc.perform(get(url)).andExpect(status().isForbidden());
        }
    }

    @Test
    @WithUserDetails("carlos.lima@exemplo.com")
    void outroProfessorVeOsAlunosDaTurmaDele() throws Exception {
        mvc.perform(get("/alunos"))
                .andExpect(content().string(containsString("Beatriz UFMG")))
                .andExpect(content().string(not(containsString("João Pereira"))));
    }

    @Test
    @WithUserDetails("empresa@exemplo.com")
    void empresaSoVeSuasVantagensEQuemResgatou() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/minhas-vantagens"));
        mvc.perform(get("/minhas-vantagens"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Almoço grátis")));
        mvc.perform(get("/minhas-vantagens/resgates"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Beatriz UFMG")))
                .andExpect(content().string(containsString("TESTE-123")));
        for (String url : new String[]{"/vantagens", "/alunos", "/empresas", "/professores", "/painel"}) {
            mvc.perform(get(url)).andExpect(status().isForbidden());
        }
    }

    @Test
    @WithUserDetails("admin@exemplo.com")
    void adminVeOsDadosGerais() throws Exception {
        mvc.perform(get("/")).andExpect(redirectedUrl("/instituicoes"));
        // instituições com seus professores
        mvc.perform(get("/instituicoes"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nova instituição")))
                .andExpect(content().string(containsString("Ana Souza")));
        mvc.perform(get("/instituicoes/nova")).andExpect(status().isOk());
        // alunos de todas as instituições, só para visualizar
        mvc.perform(get("/alunos"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("João Pereira")))
                .andExpect(content().string(containsString("Beatriz UFMG")))
                .andExpect(content().string(not(containsString("Editar"))))
                .andExpect(content().string(not(containsString("Excluir"))))
                .andExpect(content().string(not(containsString("Enviar moedas"))));
        // filtro por instituição
        Long ufmg = cursoRepository.findFirstByNome("Sistemas de Informação").orElseThrow().getInstituicao().getId();
        mvc.perform(get("/alunos").param("instituicaoId", ufmg.toString()))
                .andExpect(content().string(containsString("Beatriz UFMG")))
                .andExpect(content().string(not(containsString("João Pereira"))));
        mvc.perform(get("/professores")).andExpect(status().isOk());
        mvc.perform(get("/empresas"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Restaurante Universitário")))
                .andExpect(content().string(not(containsString("Excluir"))));
        // o admin não cria/edita alunos e empresas, não usa o catálogo e não tem painel
        for (String url : new String[]{"/alunos/novo", "/empresas/novo", "/vantagens", "/painel", "/minhas-vantagens"}) {
            mvc.perform(get(url)).andExpect(status().isForbidden());
        }
    }

    @Test
    @WithUserDetails("ana.souza@exemplo.com")
    void professorNaoVeAcoesDeAdmin() throws Exception {
        mvc.perform(get("/alunos"))
                .andExpect(content().string(not(containsString("Novo aluno"))))
                .andExpect(content().string(not(containsString("Excluir"))));
    }

    @Test
    @WithUserDetails("empresa@exemplo.com")
    void empresaNaoEditaVantagemDeOutraEmpresa() throws Exception {
        EmpresaParceira outra = new EmpresaParceira();
        outra.setNome("Outra");
        outra.setResponsavel("X");
        outra.setEmail("outra.empresa@exemplo.com");
        outra.setSenha("x");
        outra.setTelefone("1");
        outra.setEndereco("Rua");
        empresaRepository.save(outra);
        Vantagem alheia = new Vantagem();
        alheia.setNome("Vantagem alheia");
        alheia.setDescricao("D");
        alheia.setFoto("vantagens/outra.png");
        alheia.setValor(10);
        alheia.setEmpresa(outra);
        vantagemRepository.save(alheia);

        mvc.perform(get("/minhas-vantagens/{id}/editar", alheia.getId())).andExpect(status().isNotFound());
        mvc.perform(get("/minhas-vantagens"))
                .andExpect(content().string(not(containsString("Vantagem alheia"))));
    }
}
