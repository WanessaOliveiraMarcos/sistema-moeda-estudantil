package com.exemplo.app;

import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.CriterioTroca;
import com.exemplo.app.model.Curso;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.StatusTransferencia;
import com.exemplo.app.model.TransferenciaAluno;
import com.exemplo.app.model.Turma;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.CursoRepository;
import com.exemplo.app.repository.TransferenciaAlunoRepository;
import com.exemplo.app.repository.TurmaRepository;
import com.exemplo.app.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Turmas (relação professor–aluno, até 40 alunos), critérios de troca por turma e
 * envio de moedas entre alunos da mesma turma com autorização do professor.
 * Usa a turma de demonstração da professora Ana com João e Maria (CargaInicial).
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class TurmasTests {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserDetailsService usuarios;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private TurmaRepository turmaRepository;
    @Autowired
    private TransferenciaAlunoRepository transferenciaRepository;
    @Autowired
    private TransactionTemplate transacao;

    // alunos e critérios da turma são carregados sob demanda: leituras do teste precisam de uma transação
    private <T> T emTransacao(Supplier<T> leitura) {
        return transacao.execute(status -> leitura.get());
    }

    private boolean estaNaTurma(Long turmaId, String email) {
        return emTransacao(() -> turmaRepository.findById(turmaId).orElseThrow().temAluno(aluno(email).getId()));
    }

    private RequestPostProcessor como(String email) {
        return user(usuarios.loadUserByUsername(email));
    }

    private Aluno aluno(String email) {
        return (Aluno) usuarioRepository.findByEmail(email).orElseThrow();
    }

    private Professor professor(String email) {
        return (Professor) usuarioRepository.findByEmail(email).orElseThrow();
    }

    private Turma turmaDemonstracao() {
        return turmaRepository.findByProfessorIdOrderBySemestreDescNomeAsc(professor("ana.souza@exemplo.com").getId())
                .stream().filter(t -> t.getNome().equals("Laboratório de Desenvolvimento de Software"))
                .findFirst().orElseThrow();
    }

    private CriterioTroca ajudaEmExercicio() {
        return emTransacao(() -> turmaDemonstracao().getCriterios().stream()
                .filter(c -> c.getDescricao().equals("Ajuda em exercício")).findFirst().orElseThrow());
    }

    private void darSaldo(String email, int saldo) {
        Aluno aluno = aluno(email);
        aluno.setSaldoMoedas(saldo);
        alunoRepository.save(aluno);
    }

    private Turma novaTurma(String professorEmail, String nome) {
        Turma turma = new Turma();
        turma.setNome(nome);
        turma.setSemestre("2026/2");
        turma.setProfessor(professor(professorEmail));
        return turmaRepository.save(turma);
    }

    @Test
    void professorCriaTurmaECriterioEAlunoSeMatricula() throws Exception {
        mvc.perform(post("/turmas").with(como("ana.souza@exemplo.com")).with(csrf())
                        .param("nome", "Engenharia de Requisitos").param("semestre", "2026/2"))
                .andExpect(status().is3xxRedirection());
        Turma requisitos = turmaRepository.findByProfessorIdOrderBySemestreDescNomeAsc(
                        professor("ana.souza@exemplo.com").getId()).stream()
                .filter(t -> t.getNome().equals("Engenharia de Requisitos")).findFirst().orElseThrow();

        mvc.perform(post("/turmas/{id}/criterios", requisitos.getId()).with(como("ana.souza@exemplo.com")).with(csrf())
                        .param("descricao", "Revisão de documento").param("valorMaximo", "30"))
                .andExpect(redirectedUrl("/turmas/" + requisitos.getId()));

        // o aluno vê a turma como disponível e se matricula
        mvc.perform(get("/minhas-turmas").with(como("aluno@exemplo.com")))
                .andExpect(content().string(containsString("Engenharia de Requisitos")));
        mvc.perform(post("/minhas-turmas/{id}/matricular", requisitos.getId()).with(como("aluno@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("mensagem", containsString("Matrícula feita")));
        assertThat(estaNaTurma(requisitos.getId(), "aluno@exemplo.com")).isTrue();

        // segunda matrícula na mesma turma é recusada; os critérios aparecem para o aluno
        mvc.perform(post("/minhas-turmas/{id}/matricular", requisitos.getId()).with(como("aluno@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("já está matriculado")));
        mvc.perform(get("/minhas-turmas").with(como("aluno@exemplo.com")))
                .andExpect(content().string(containsString("Revisão de documento")));
    }

    @Test
    void alunoNaoSeMatriculaEmTurmaDeOutraInstituicao() throws Exception {
        Turma daUfmg = novaTurma("carlos.lima@exemplo.com", "Estruturas de Dados");
        mvc.perform(post("/minhas-turmas/{id}/matricular", daUfmg.getId()).with(como("aluno@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("outra instituição")));
    }

    @Test
    void turmaTemLimiteDe40Alunos() throws Exception {
        Turma cheia = novaTurma("ana.souza@exemplo.com", "Turma lotada");
        Curso curso = cursoRepository.findFirstByNome("Engenharia de Software").orElseThrow();
        for (int i = 0; i < Turma.LIMITE_ALUNOS; i++) {
            Aluno a = new Aluno();
            a.setNome("Aluno " + i);
            a.setEmail("lotacao" + i + "@exemplo.com");
            a.setSenha("x");
            a.setCpf(String.format("880000%05d", i));
            a.setRg("1");
            a.setEndereco("Rua");
            a.setInstituicao(curso.getInstituicao());
            a.setCurso(curso);
            cheia.getAlunos().add(alunoRepository.save(a));
        }
        turmaRepository.save(cheia);

        mvc.perform(post("/minhas-turmas/{id}/matricular", cheia.getId()).with(como("aluno@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("já tem 40 alunos")));
        assertThat(emTransacao(() -> turmaRepository.findById(cheia.getId()).orElseThrow().getAlunos().size()))
                .isEqualTo(40);
        assertThat(estaNaTurma(cheia.getId(), "aluno@exemplo.com")).isFalse();
    }

    @Test
    void envioEntreColegasSoAconteceDepoisDaAutorizacao() throws Exception {
        darSaldo("aluno@exemplo.com", 100);
        darSaldo("aluna2@exemplo.com", 0);
        Turma turma = turmaDemonstracao();

        mvc.perform(post("/transferencias").with(como("aluno@exemplo.com")).with(csrf())
                        .param("turmaId", turma.getId().toString())
                        .param("destinatarioId", aluno("aluna2@exemplo.com").getId().toString())
                        .param("criterioId", ajudaEmExercicio().getId().toString())
                        .param("valor", "15")
                        .param("mensagem", "Valeu pela ajuda na lista"))
                .andExpect(redirectedUrl("/transferencias"));

        // pedido pendente: nenhuma moeda mudou de dono
        assertThat(aluno("aluno@exemplo.com").getSaldoMoedas()).isEqualTo(100);
        assertThat(aluno("aluna2@exemplo.com").getSaldoMoedas()).isZero();
        TransferenciaAluno pedido = transferenciaRepository.buscarDoAluno(aluno("aluno@exemplo.com").getId()).get(0);
        assertThat(pedido.getStatus()).isEqualTo(StatusTransferencia.PENDENTE);

        // professor de outra turma não decide
        mvc.perform(post("/autorizacoes/{id}/aprovar", pedido.getId()).with(como("carlos.lima@exemplo.com")).with(csrf()))
                .andExpect(status().isNotFound());

        mvc.perform(get("/autorizacoes").with(como("ana.souza@exemplo.com")))
                .andExpect(content().string(containsString("Valeu pela ajuda na lista")));
        mvc.perform(post("/autorizacoes/{id}/aprovar", pedido.getId()).with(como("ana.souza@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("mensagem", containsString("15 moedas transferidas")));

        assertThat(aluno("aluno@exemplo.com").getSaldoMoedas()).isEqualTo(85);
        assertThat(aluno("aluna2@exemplo.com").getSaldoMoedas()).isEqualTo(15);
        assertThat(transferenciaRepository.findById(pedido.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusTransferencia.APROVADA);

        // aprovar de novo não transfere outra vez
        mvc.perform(post("/autorizacoes/{id}/aprovar", pedido.getId()).with(como("ana.souza@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("já foi")));
        assertThat(aluno("aluna2@exemplo.com").getSaldoMoedas()).isEqualTo(15);

        // aparece no extrato dos dois
        mvc.perform(get("/extrato").with(como("aluna2@exemplo.com")))
                .andExpect(content().string(containsString("Recebido de colega")));
        mvc.perform(get("/extrato").with(como("aluno@exemplo.com")))
                .andExpect(content().string(containsString("Enviado a colega")));
    }

    @Test
    void envioEntreColegasRespeitaTurmaECriterio() throws Exception {
        darSaldo("aluno@exemplo.com", 100);
        Turma turma = turmaDemonstracao();
        String colega = aluno("aluna2@exemplo.com").getId().toString();

        // acima do máximo do critério (20)
        mvc.perform(post("/transferencias").with(como("aluno@exemplo.com")).with(csrf())
                        .param("turmaId", turma.getId().toString()).param("destinatarioId", colega)
                        .param("criterioId", ajudaEmExercicio().getId().toString()).param("valor", "21"))
                .andExpect(model().attributeHasFieldErrors("transferencia", "valor"));

        // colega que não está na turma
        Curso curso = cursoRepository.findFirstByNome("Engenharia de Software").orElseThrow();
        Aluno deFora = new Aluno();
        deFora.setNome("Fora da Turma");
        deFora.setEmail("fora.turma@exemplo.com");
        deFora.setSenha("x");
        deFora.setCpf("16180339805");
        deFora.setRg("1");
        deFora.setEndereco("Rua");
        deFora.setInstituicao(curso.getInstituicao());
        deFora.setCurso(curso);
        alunoRepository.save(deFora);
        mvc.perform(post("/transferencias").with(como("aluno@exemplo.com")).with(csrf())
                        .param("turmaId", turma.getId().toString()).param("destinatarioId", deFora.getId().toString())
                        .param("criterioId", ajudaEmExercicio().getId().toString()).param("valor", "5"))
                .andExpect(model().attributeHasFieldErrors("transferencia", "destinatarioId"));

        // quem não está na turma não pede envio nela
        mvc.perform(post("/transferencias").with(como("fora.turma@exemplo.com")).with(csrf())
                        .param("turmaId", turma.getId().toString()).param("destinatarioId", colega)
                        .param("criterioId", ajudaEmExercicio().getId().toString()).param("valor", "5"))
                .andExpect(model().attributeHasFieldErrors("transferencia", "turmaId"));
    }

    @Test
    void professorRecusaOuPedidoCaiPorFaltaDeSaldo() throws Exception {
        darSaldo("aluno@exemplo.com", 20);
        darSaldo("aluna2@exemplo.com", 0);
        Turma turma = turmaDemonstracao();
        String colega = aluno("aluna2@exemplo.com").getId().toString();
        String criterio = ajudaEmExercicio().getId().toString();
        for (String valor : new String[]{"10", "20"}) {
            mvc.perform(post("/transferencias").with(como("aluno@exemplo.com")).with(csrf())
                    .param("turmaId", turma.getId().toString()).param("destinatarioId", colega)
                    .param("criterioId", criterio).param("valor", valor));
        }
        var pedidos = transferenciaRepository.buscarDoAluno(aluno("aluno@exemplo.com").getId()).stream()
                .filter(t -> t.getStatus() == StatusTransferencia.PENDENTE).toList();
        TransferenciaAluno de20 = pedidos.stream().filter(t -> t.getValor() == 20).findFirst().orElseThrow();
        TransferenciaAluno de10 = pedidos.stream().filter(t -> t.getValor() == 10).findFirst().orElseThrow();

        mvc.perform(post("/autorizacoes/{id}/recusar", de10.getId()).with(como("ana.souza@exemplo.com")).with(csrf())
                        .param("motivo", "Use o critério de monitoria"))
                .andExpect(flash().attribute("mensagem", containsString("recusado")));
        assertThat(transferenciaRepository.findById(de10.getId()).orElseThrow().getMotivoRecusa())
                .isEqualTo("Use o critério de monitoria");

        // o aluno gastou as moedas depois do pedido: a aprovação vira recusa automática
        darSaldo("aluno@exemplo.com", 5);
        mvc.perform(post("/autorizacoes/{id}/aprovar", de20.getId()).with(como("ana.souza@exemplo.com")).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("saldo insuficiente")));
        assertThat(transferenciaRepository.findById(de20.getId()).orElseThrow().getStatus())
                .isEqualTo(StatusTransferencia.RECUSADA);
        assertThat(aluno("aluno@exemplo.com").getSaldoMoedas()).isEqualTo(5);
        assertThat(aluno("aluna2@exemplo.com").getSaldoMoedas()).isZero();
    }

    @Test
    void cadaPerfilSoAcessaSuasTelasDeTurma() throws Exception {
        for (String url : new String[]{"/turmas", "/autorizacoes"}) {
            mvc.perform(get(url).with(como("aluno@exemplo.com"))).andExpect(status().isForbidden());
            mvc.perform(get(url).with(como("ana.souza@exemplo.com"))).andExpect(status().isOk());
        }
        for (String url : new String[]{"/minhas-turmas", "/transferencias"}) {
            mvc.perform(get(url).with(como("ana.souza@exemplo.com"))).andExpect(status().isForbidden());
            mvc.perform(get(url).with(como("aluno@exemplo.com"))).andExpect(status().isOk());
        }
        // professor não abre turma de outro professor
        Turma daAna = turmaDemonstracao();
        mvc.perform(get("/turmas/{id}", daAna.getId()).with(como("carlos.lima@exemplo.com")))
                .andExpect(status().isNotFound());
    }
}
