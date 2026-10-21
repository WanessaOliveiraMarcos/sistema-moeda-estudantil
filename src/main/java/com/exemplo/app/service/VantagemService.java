package com.exemplo.app.service;

import com.exemplo.app.dto.VantagemForm;
import com.exemplo.app.exception.RegraNegocioException;
import com.exemplo.app.model.TrocaVantagem;
import com.exemplo.app.model.Vantagem;
import com.exemplo.app.repository.TrocaVantagemRepository;
import com.exemplo.app.repository.VantagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VantagemService {

    private static final String PASTA_FOTOS = "vantagens";

    private final VantagemRepository vantagemRepository;
    private final TrocaVantagemRepository trocaRepository;
    private final EmpresaParceiraService empresaService;
    private final StorageService storageService;

    public List<Vantagem> listarPorEmpresa(Long empresaId) {
        return vantagemRepository.findByEmpresaIdOrderByNomeAsc(empresaId);
    }

    /** Resgates das vantagens da empresa, com o aluno que resgatou (mais recentes primeiro). */
    public List<TrocaVantagem> listarResgates(Long empresaId) {
        return trocaRepository.findByVantagemEmpresaIdOrderByDataHoraDesc(empresaId);
    }

    /** Catálogo para os alunos (US16). ordem: "nome", "menor" ou "maior" custo. */
    public List<Vantagem> catalogo(String busca, Long empresaId, String ordem) {
        Sort sort = switch (ordem == null ? "" : ordem) {
            case "menor" -> Sort.by("valor").and(Sort.by("nome"));
            case "maior" -> Sort.by(Sort.Direction.DESC, "valor").and(Sort.by("nome"));
            default -> Sort.by("nome");
        };
        String termo = busca == null || busca.isBlank() ? null : busca.trim();
        return vantagemRepository.buscarCatalogo(termo, empresaId, sort);
    }

    public Vantagem buscar(Long empresaId, Long id) {
        return vantagemRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vantagem não encontrada"));
    }

    @Transactional
    public Vantagem salvar(Long empresaId, VantagemForm form) {
        Vantagem vantagem = form.getId() == null ? new Vantagem() : buscar(empresaId, form.getId());

        MultipartFile arquivo = form.getArquivo();
        boolean novaFoto = arquivo != null && !arquivo.isEmpty();
        if (!novaFoto && vantagem.getId() == null) {
            throw new RegraNegocioException("arquivo", "Envie uma foto da vantagem");
        }

        vantagem.setEmpresa(empresaService.buscar(empresaId));
        vantagem.setNome(form.getNome().trim());
        vantagem.setDescricao(form.getDescricao().trim());
        vantagem.setValor(form.getValor());

        if (novaFoto) {
            String fotoAnterior = vantagem.getFoto();
            vantagem.setFoto(salvarFoto(arquivo));
            storageService.excluirSeRollback(vantagem.getFoto());
            storageService.excluirAposCommit(fotoAnterior);
        }
        return vantagemRepository.save(vantagem);
    }

    @Transactional
    public void excluir(Long empresaId, Long id) {
        Vantagem vantagem = buscar(empresaId, id);
        vantagemRepository.delete(vantagem);
        vantagemRepository.flush();
        storageService.excluirAposCommit(vantagem.getFoto());
    }

    private String salvarFoto(MultipartFile arquivo) {
        try {
            return storageService.salvarImagem(arquivo, PASTA_FOTOS);
        } catch (IllegalArgumentException e) {
            throw new RegraNegocioException("arquivo", e.getMessage());
        }
    }
}
