package com.exemplo.app.service;

import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.Usuario;
import com.exemplo.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Regras de login compartilhadas por todos os perfis. */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public void definirEmail(Usuario usuario, String email) {
        String normalizado = email.trim().toLowerCase();
        boolean emUso = usuario.getId() == null
                ? usuarioRepository.existsByEmail(normalizado)
                : usuarioRepository.existsByEmailAndIdNot(normalizado, usuario.getId());
        if (emUso) {
            throw new RegraNegocioException("email", "E-mail já cadastrado");
        }
        usuario.setEmail(normalizado);
    }

    public void definirSenha(Usuario usuario, String senha) {
        if (senha == null || senha.isBlank()) {
            if (usuario.getId() == null) {
                throw new RegraNegocioException("senha", "Informe uma senha");
            }
            return;
        }
        if (senha.length() < 6) {
            throw new RegraNegocioException("senha", "A senha deve ter pelo menos 6 caracteres");
        }
        usuario.setSenha(passwordEncoder.encode(senha));
    }
}
