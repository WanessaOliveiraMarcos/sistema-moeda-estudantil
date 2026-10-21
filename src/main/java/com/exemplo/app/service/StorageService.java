package com.exemplo.app.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Grava arquivos enviados em disco, dentro de app.storage.dir. */
@Slf4j
@Service
public class StorageService {

    // formato detectado pelo conteúdo do arquivo -> extensão gravada
    private static final Map<String, String> FORMATOS_IMAGEM = Map.of("jpeg", "jpg", "png", "png", "gif", "gif");

    @Getter
    private final Path raiz;

    public StorageService(@Value("${app.storage.dir}") String diretorio) throws IOException {
        this.raiz = Paths.get(diretorio).toAbsolutePath().normalize();
        Files.createDirectories(raiz);
    }

    /**
     * Salva a imagem em {raiz}/{pasta}/ com nome aleatório e devolve o caminho
     * relativo ao storage, ex.: "vantagens/3f2a9c...png".
     */
    public String salvarImagem(MultipartFile arquivo, String pasta) {
        String caminho = pasta + "/" + UUID.randomUUID() + "." + extensaoDaImagem(arquivo);
        Path destino = resolver(caminho);
        try {
            Files.createDirectories(destino.getParent());
            arquivo.transferTo(destino);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar o arquivo no storage", e);
        }
        return caminho;
    }

    public void excluir(String caminho) {
        if (caminho == null || caminho.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolver(caminho));
        } catch (IOException e) {
            log.warn("Não foi possível excluir {} do storage", caminho, e);
        }
    }

    /** Exclui o arquivo só depois que a transação atual for confirmada. */
    public void excluirAposCommit(String caminho) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                excluir(caminho);
            }
        });
    }

    /** Exclui o arquivo se a transação atual for desfeita, para não deixar arquivo órfão. */
    public void excluirSeRollback(String caminho) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    excluir(caminho);
                }
            }
        });
    }

    private Path resolver(String caminho) {
        Path arquivo = raiz.resolve(caminho).normalize();
        if (!arquivo.startsWith(raiz)) {
            throw new IllegalArgumentException("Caminho fora do storage: " + caminho);
        }
        return arquivo;
    }

    // confere o conteúdo do arquivo, não a extensão nem o Content-Type informados pelo navegador
    private String extensaoDaImagem(MultipartFile arquivo) {
        try (ImageInputStream entrada = ImageIO.createImageInputStream(arquivo.getInputStream())) {
            if (entrada != null) {
                Iterator<ImageReader> leitores = ImageIO.getImageReaders(entrada);
                if (leitores.hasNext()) {
                    String extensao = FORMATOS_IMAGEM.get(leitores.next().getFormatName().toLowerCase());
                    if (extensao != null) {
                        return extensao;
                    }
                }
            }
        } catch (IOException e) {
            log.debug("Arquivo enviado não pôde ser lido como imagem", e);
        }
        throw new IllegalArgumentException("Envie uma imagem JPG, PNG ou GIF");
    }
}
