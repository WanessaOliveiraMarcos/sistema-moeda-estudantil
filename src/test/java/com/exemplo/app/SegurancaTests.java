package com.exemplo.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void paginasExigemLogin() throws Exception {
        for (String url : new String[]{"/", "/alunos", "/alunos/novo", "/empresas", "/vantagens", "/professores"}) {
            mvc.perform(get(url)).andExpect(redirectedUrlPattern("**/login"));
        }
    }

    @Test
    void loginECadastroSaoPublicos() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk());
        mvc.perform(get("/cadastro/aluno")).andExpect(status().isOk());
        mvc.perform(get("/cadastro/empresa")).andExpect(status().isOk());
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void loginComProfessorPreCadastrado() throws Exception {
        // e-mail em maiúsculas também deve funcionar
        mvc.perform(formLogin("/login").user("email", "Ana.Souza@exemplo.com").password("senha", "123456"))
                .andExpect(authenticated().withRoles("PROFESSOR"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginComUsuariosDeDemonstracao() throws Exception {
        mvc.perform(formLogin("/login").user("email", "aluno@exemplo.com").password("senha", "123456"))
                .andExpect(authenticated().withRoles("ALUNO"));
        mvc.perform(formLogin("/login").user("email", "empresa@exemplo.com").password("senha", "123456"))
                .andExpect(authenticated().withRoles("EMPRESA"));
    }

    @Test
    void senhaErradaVoltaParaLoginComErro() throws Exception {
        mvc.perform(formLogin("/login").user("email", "ana.souza@exemplo.com").password("senha", "errada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
    }

    @Test
    void cadastroDeAlunoVaiParaOLogin() throws Exception {
        mvc.perform(post("/cadastro/aluno").with(csrf())
                        .param("nome", "Novo Aluno").param("email", "novo.aluno@exemplo.com")
                        .param("cpf", "826.734.270-23").param("rg", "MG-1").param("endereco", "Rua")
                        .param("instituicaoId", "1").param("cursoId", "1").param("senha", "123456"))
                .andExpect(redirectedUrl("/login?cadastrado"));
        mvc.perform(formLogin("/login").user("email", "novo.aluno@exemplo.com").password("senha", "123456"))
                .andExpect(authenticated().withRoles("ALUNO"));
    }

    @Test
    void cadastroComAlguemLogadoEncerraASessaoEVaiParaOLogin() throws Exception {
        MockHttpSession sessao = (MockHttpSession) mvc
                .perform(formLogin("/login").user("email", "admin@exemplo.com").password("senha", "123456"))
                .andReturn().getRequest().getSession();

        mvc.perform(post("/cadastro/aluno").session(sessao).with(csrf())
                        .param("nome", "Outro Aluno").param("email", "outro.aluno@exemplo.com")
                        .param("cpf", "617.874.460-93").param("rg", "MG-2").param("endereco", "Rua")
                        .param("instituicaoId", "1").param("cursoId", "1").param("senha", "123456"))
                .andExpect(redirectedUrl("/login?cadastrado"))
                .andExpect(unauthenticated());
        assertThat(sessao.isInvalid()).isTrue();
    }

    @Test
    void cadastroPublicoNaoAlteraContaExistente() throws Exception {
        // tenta "editar" o professor 1 pelo cadastro público: o id é ignorado e o e-mail já existe
        mvc.perform(post("/cadastro/empresa").with(csrf())
                        .param("id", "1").param("nome", "Invasor").param("responsavel", "X")
                        .param("email", "ana.souza@exemplo.com").param("telefone", "1")
                        .param("endereco", "Rua").param("senha", "123456"))
                .andExpect(status().isOk()); // volta ao formulário com "E-mail já cadastrado"
    }
}
