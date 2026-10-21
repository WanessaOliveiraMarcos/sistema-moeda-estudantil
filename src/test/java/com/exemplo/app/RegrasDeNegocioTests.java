package com.exemplo.app;

import com.exemplo.app.model.Aluno;
import com.exemplo.app.model.Curso;
import com.exemplo.app.model.EmpresaParceira;
import com.exemplo.app.model.Professor;
import com.exemplo.app.model.SaldoMoedasProfessor;
import com.exemplo.app.model.Vantagem;
import com.exemplo.app.repository.AlunoRepository;
import com.exemplo.app.repository.CursoRepository;
import com.exemplo.app.repository.DepartamentoRepository;
import com.exemplo.app.repository.ProfessorRepository;
import com.exemplo.app.repository.SaldoMoedasProfessorRepository;
import com.exemplo.app.repository.TransacaoMoedasRepository;
import com.exemplo.app.repository.TrocaVantagemRepository;
import com.exemplo.app.repository.UsuarioRepository;
import com.exemplo.app.repository.VantagemRepository;
import com.exemplo.app.service.ProfessorService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

/**
 * Regras de negócio: professor distribui moedas, aluno recebe e troca por vantagens,
 * ambos veem o saldo, empresa vê quem resgatou e o admin cadastra instituições com professores.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class RegrasDeNegocioTests {

    @MockitoBean
    private JavaMailSender mailSender;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AlunoRepository alunoRepository;
    @Autowired
    private ProfessorRepository professorRepository;
    @Autowired
    private CursoRepository cursoRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private SaldoMoedasProfessorRepository saldoRepository;
    @Autowired
    private TransacaoMoedasRepository transacaoRepository;
    @Autowired
    private VantagemRepository vantagemRepository;
    @Autowired
    private TrocaVantagemRepository trocaRepository;
    @Autowired
    private ProfessorService professorService;

    private Aluno aluno() {
        return (Aluno) usuarioRepository.findByEmail("aluno@exemplo.com").orElseThrow();
    }

    private int saldoProfessor(String email) {
        Professor professor = (Professor) usuarioRepository.findByEmail(email).orElseThrow();
        return professorService.saldoDoSemestre(professor).getValorFinal();
    }

    private Vantagem vantagem(String nome, int valor) {
        Vantagem vantagem = new Vantagem();
        vantagem.setNome(nome);
        vantagem.setDescricao("Descrição");
        vantagem.setFoto("vantagens/teste.png");
        vantagem.setValor(valor);
        vantagem.setEmpresa((EmpresaParceira) usuarioRepository.findByEmail("empresa@exemplo.com").orElseThrow());
        return vantagemRepository.save(vantagem);
    }

    @Test
    @WithUserDetails("ana.souza@exemplo.com")
    void professorEnviaMoedasEAlunoRecebeComEmail() throws Exception {
        int saldoAluno = aluno().getSaldoMoedas();
        int saldoProfessor = saldoProfessor("ana.souza@exemplo.com");
        clearInvocations(mailSender);

        mvc.perform(post("/moedas/enviar").with(csrf())
                        .param("alunoId", aluno().getId().toString())
                        .param("valor", "150")
                        .param("motivo", "Ótima apresentação"))
                .andExpect(redirectedUrl("/extrato"));

        assertThat(aluno().getSaldoMoedas()).isEqualTo(saldoAluno + 150);
        assertThat(saldoProfessor("ana.souza@exemplo.com")).isEqualTo(saldoProfessor - 150);
        assertThat(transacaoRepository.findByAlunoIdOrderByDataHoraDesc(aluno().getId()))
                .anyMatch(t -> t.getMotivo().equals("Ótima apresentação"));

        // e-mail ao aluno, enviado depois do commit e em segundo plano
        ArgumentCaptor<SimpleMailMessage> email = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(5000)).send(email.capture());
        assertThat(email.getValue().getTo()).containsExactly("aluno@exemplo.com");
        assertThat(email.getValue().getText()).contains("Ana Souza", "150", "Ótima apresentação");

        mvc.perform(get("/extrato"))
                .andExpect(content().string(containsString("Para João Pereira")))
                .andExpect(content().string(containsString("Ótima apresentação")));
    }

    @Test
    @WithUserDetails("ana.souza@exemplo.com")
    void envioRespeitaSaldoMotivoEInstituicao() throws Exception {
        int saldo = saldoProfessor("ana.souza@exemplo.com");
        mvc.perform(post("/moedas/enviar").with(csrf())
                        .param("alunoId", aluno().getId().toString())
                        .param("valor", String.valueOf(saldo + 1))
                        .param("motivo", "Muito"))
                .andExpect(model().attributeHasFieldErrors("envio", "valor"));

        mvc.perform(post("/moedas/enviar").with(csrf())
                        .param("alunoId", aluno().getId().toString())
                        .param("valor", "10")
                        .param("motivo", " "))
                .andExpect(model().attributeHasFieldErrors("envio", "motivo"));

        // aluno de outra instituição (UFMG)
        Curso sistemas = cursoRepository.findFirstByNome("Sistemas de Informação").orElseThrow();
        Aluno daUfmg = new Aluno();
        daUfmg.setNome("Aluno UFMG");
        daUfmg.setEmail("outro.ufmg@exemplo.com");
        daUfmg.setSenha("x");
        daUfmg.setCpf("15350946056");
        daUfmg.setRg("1");
        daUfmg.setEndereco("Rua");
        daUfmg.setInstituicao(sistemas.getInstituicao());
        daUfmg.setCurso(sistemas);
        alunoRepository.save(daUfmg);
        mvc.perform(post("/moedas/enviar").with(csrf())
                        .param("alunoId", daUfmg.getId().toString())
                        .param("valor", "10")
                        .param("motivo", "Tentativa"))
                .andExpect(model().attributeHasFieldErrors("envio", "alunoId"));

        assertThat(saldoProfessor("ana.souza@exemplo.com")).isEqualTo(saldo);
    }

    @Test
    @WithUserDetails("aluno@exemplo.com")
    void alunoResgataVantagemEEmpresaVeQuemComprou() throws Exception {
        Aluno aluno = aluno();
        aluno.setSaldoMoedas(aluno.getSaldoMoedas() + 100);
        alunoRepository.save(aluno);
        int saldo = aluno().getSaldoMoedas();
        Vantagem pizza = vantagem("Pizza no RU", 70);
        clearInvocations(mailSender);

        mvc.perform(post("/vantagens/{id}/resgatar", pizza.getId()).with(csrf()))
                .andExpect(redirectedUrl("/extrato"))
                .andExpect(flash().attribute("mensagem", containsString("MOE-")));

        assertThat(aluno().getSaldoMoedas()).isEqualTo(saldo - 70);
        var troca = trocaRepository.findByAlunoIdOrderByDataHoraDesc(aluno.getId()).get(0);
        assertThat(troca.getValor()).isEqualTo(70);
        assertThat(troca.getCodigo()).matches("MOE-[A-Z2-9]{4}-[A-Z2-9]{4}");

        // cupom para o aluno e aviso para a empresa, os dois com o código
        ArgumentCaptor<SimpleMailMessage> emails = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, timeout(5000).times(2)).send(emails.capture());
        List<SimpleMailMessage> enviados = emails.getAllValues();
        assertThat(enviados).extracting(m -> m.getTo()[0])
                .containsExactlyInAnyOrder("aluno@exemplo.com", "empresa@exemplo.com");
        assertThat(enviados).allMatch(m -> m.getText().contains(troca.getCodigo()));

        mvc.perform(get("/extrato"))
                .andExpect(content().string(containsString("Pizza no RU")))
                .andExpect(content().string(containsString(troca.getCodigo())));
    }

    @Test
    @WithUserDetails("aluno@exemplo.com")
    void resgateSemSaldoNaoAcontece() throws Exception {
        int saldo = aluno().getSaldoMoedas();
        Vantagem cara = vantagem("Notebook", saldo + 1);

        mvc.perform(post("/vantagens/{id}/resgatar", cara.getId()).with(csrf()))
                .andExpect(redirectedUrl("/vantagens"))
                .andExpect(flash().attribute("erro", containsString("Saldo insuficiente")));

        assertThat(aluno().getSaldoMoedas()).isEqualTo(saldo);
        assertThat(trocaRepository.findByVantagemEmpresaIdOrderByDataHoraDesc(cara.getEmpresa().getId()))
                .noneMatch(t -> t.getVantagem().getId().equals(cara.getId()));
    }

    @Test
    @WithUserDetails("aluno@exemplo.com")
    void alunoVeSaldoNoMenuENoCatalogo() throws Exception {
        mvc.perform(get("/vantagens"))
                .andExpect(content().string(containsString("class=\"saldo\"")))
                .andExpect(content().string(containsString("Seu saldo:")));
    }

    @Test
    void saldoDoSemestreAcumulaOQueSobrou() {
        Professor professor = new Professor();
        professor.setNome("Professor Acúmulo");
        professor.setCpf("74125896364");
        professor.setEmail("acumulo@exemplo.com");
        professor.setSenha("x");
        professor.setDepartamento(departamentoRepository.findAll().get(0));
        professorRepository.save(professor);

        SaldoMoedasProfessor anterior = new SaldoMoedasProfessor();
        anterior.setProfessor(professor);
        anterior.setSemestre("2000/1");
        anterior.setValorInicial(1000);
        anterior.setValorFinal(300);
        saldoRepository.save(anterior);

        SaldoMoedasProfessor atual = professorService.saldoDoSemestre(professor);
        assertThat(atual.getSemestre()).isEqualTo(SaldoMoedasProfessor.semestreAtual());
        assertThat(atual.getValorInicial()).isEqualTo(1300);
        assertThat(atual.getValorFinal()).isEqualTo(1300);
        // consultar de novo não credita outra vez
        assertThat(professorService.saldoDoSemestre(professor).getValorFinal()).isEqualTo(1300);
    }

    @Test
    @WithUserDetails("admin@exemplo.com")
    void adminCadastraInstituicaoComProfessores() throws Exception {
        mvc.perform(post("/instituicoes").with(csrf())
                        .param("nome", "CEFET-MG").param("endereco", "Av. Amazonas, 5253")
                        .param("cursos[0].nome", "Engenharia de Computação").param("cursos[0].semestres", "10")
                        .param("professores[0].nome", "Rita Alves").param("professores[0].cpf", "826.734.270-23")
                        .param("professores[0].email", "Rita@CEFET.exemplo.com")
                        .param("professores[0].departamento", "Computação").param("professores[0].senha", "senha123")
                        .param("professores[1].nome", "Paulo Dias").param("professores[1].cpf", "61787446093")
                        .param("professores[1].email", "paulo@cefet.exemplo.com")
                        .param("professores[1].departamento", "computação").param("professores[1].senha", "senha123"))
                .andExpect(redirectedUrl("/instituicoes"));

        Professor rita = (Professor) usuarioRepository.findByEmail("rita@cefet.exemplo.com").orElseThrow();
        Professor paulo = (Professor) usuarioRepository.findByEmail("paulo@cefet.exemplo.com").orElseThrow();
        assertThat(rita.getDepartamento().getInstituicao().getNome()).isEqualTo("CEFET-MG");
        // "Computação" e "computação" viram um único departamento
        assertThat(paulo.getDepartamento().getId()).isEqualTo(rita.getDepartamento().getId());
        assertThat(professorService.saldoDoSemestre(rita).getValorFinal()).isEqualTo(1000);
        assertThat(cursoRepository.findFirstByNome("Engenharia de Computação")).isPresent();

        // CPF repetido na lista: nada é gravado
        mvc.perform(post("/instituicoes").with(csrf())
                        .param("nome", "Outra").param("endereco", "Rua")
                        .param("cursos[0].nome", "Curso").param("cursos[0].semestres", "8")
                        .param("professores[0].nome", "A").param("professores[0].cpf", "35815687057")
                        .param("professores[0].email", "a@outra.exemplo.com")
                        .param("professores[0].departamento", "D").param("professores[0].senha", "senha123")
                        .param("professores[1].nome", "B").param("professores[1].cpf", "358.156.870-57")
                        .param("professores[1].email", "b@outra.exemplo.com")
                        .param("professores[1].departamento", "D").param("professores[1].senha", "senha123"))
                .andExpect(model().attributeHasFieldErrors("instituicao", "professores[1].cpf"));
        assertThat(usuarioRepository.existsByEmail("a@outra.exemplo.com")).isFalse();
    }
}
