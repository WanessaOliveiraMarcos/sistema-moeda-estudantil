package com.exemplo.app.service;

import com.exemplo.app.dto.EmpresaParceiraForm;
import com.exemplo.app.model.EmpresaParceira;
import com.exemplo.app.repository.EmpresaParceiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaParceiraService {

    private final EmpresaParceiraRepository empresaRepository;
    private final UsuarioService usuarioService;
    private final StorageService storageService;

    public List<EmpresaParceira> listar() {
        return empresaRepository.findAllByOrderByNomeAsc();
    }

    public List<EmpresaParceira> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return listar();
        }
        return empresaRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
    }

    public EmpresaParceira buscar(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Empresa não encontrada"));
    }

    @Transactional
    public EmpresaParceira salvar(EmpresaParceiraForm form) {
        EmpresaParceira empresa = form.getId() == null ? new EmpresaParceira() : buscar(form.getId());

        usuarioService.definirEmail(empresa, form.getEmail());
        usuarioService.definirSenha(empresa, form.getSenha());
        empresa.setNome(form.getNome().trim());
        empresa.setResponsavel(form.getResponsavel().trim());
        empresa.setTelefone(form.getTelefone().trim());
        empresa.setEndereco(form.getEndereco().trim());
        return empresaRepository.save(empresa);
    }

    @Transactional
    public void excluir(Long id) {
        EmpresaParceira empresa = buscar(id);
        empresaRepository.delete(empresa);
        empresaRepository.flush();
        // as vantagens são excluídas em cascata; as fotos saem do storage após o commit
        empresa.getVantagens().forEach(vantagem -> storageService.excluirAposCommit(vantagem.getFoto()));
    }
}
