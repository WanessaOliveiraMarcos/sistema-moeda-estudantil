package com.exemplo.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

// habilita @Async (envio de e-mails em segundo plano)
@Configuration
@EnableAsync
public class AsyncConfig {
}
