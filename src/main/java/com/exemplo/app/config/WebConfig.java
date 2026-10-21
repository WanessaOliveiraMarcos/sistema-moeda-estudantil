package com.exemplo.app.config;

import com.exemplo.app.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StorageService storageService;

    // serve os arquivos do storage em /storage/**, ex.: /storage/vantagens/3f2a...png
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/storage/**")
                .addResourceLocations(storageService.getRaiz().toUri().toString());
    }
}
