package com.exemplo.app.security;

import com.exemplo.app.model.Perfil;
import com.exemplo.app.model.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuário autenticado guardado na sessão. O perfil vira a role ROLE_ALUNO, ROLE_PROFESSOR, ROLE_EMPRESA ou ROLE_ADMIN. */
@Getter
public class UsuarioLogado implements UserDetails {

    private final Long id;
    private final String email;
    private final String senha;
    private final String nome;
    private final Perfil perfil;

    public UsuarioLogado(Usuario usuario) {
        this.id = usuario.getId();
        this.email = usuario.getEmail();
        this.senha = usuario.getSenha();
        this.nome = usuario.getNome();
        this.perfil = usuario.getPerfil();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return senha;
    }
}
