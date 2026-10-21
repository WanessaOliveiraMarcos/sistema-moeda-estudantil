package com.exemplo.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // sem login: tela de login, auto cadastro (US03/US06) e arquivos do layout
                        .requestMatchers("/login", "/cadastro/**", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/fonts/**").permitAll()
                        // "/" redireciona para a área do perfil; fotos aparecem para aluno e empresa
                        .requestMatchers("/", "/storage/**").authenticated()
                        // aluno: catálogo de vantagens com seus preços e resgate
                        .requestMatchers(HttpMethod.GET, "/vantagens").hasRole("ALUNO")
                        .requestMatchers(HttpMethod.POST, "/vantagens/*/resgatar").hasRole("ALUNO")
                        // professor: alunos das suas turmas (filtro no AlunoController; admin vê todos) e envio de moedas
                        .requestMatchers(HttpMethod.GET, "/alunos").hasAnyRole("PROFESSOR", "ADMIN")
                        .requestMatchers("/moedas/enviar").hasRole("PROFESSOR")
                        // professor: suas turmas (com critérios de troca) e autorização dos envios entre alunos
                        .requestMatchers("/turmas/**", "/autorizacoes/**").hasRole("PROFESSOR")
                        // aluno: matrícula nas turmas e envio de moedas a colegas de turma
                        .requestMatchers("/minhas-turmas/**", "/transferencias/**").hasRole("ALUNO")
                        // saldo e movimentações de quem tem moedas
                        .requestMatchers("/extrato").hasAnyRole("ALUNO", "PROFESSOR")
                        // empresa: suas vantagens e os alunos que as resgataram (empresa vem do login)
                        .requestMatchers("/minhas-vantagens/**").hasRole("EMPRESA")
                        // admin: cadastra instituições com seus professores e visualiza professores e empresas
                        .requestMatchers("/instituicoes/**", "/professores", "/empresas").hasRole("ADMIN")
                        .anyRequest().denyAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?erro"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?saiu"));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
