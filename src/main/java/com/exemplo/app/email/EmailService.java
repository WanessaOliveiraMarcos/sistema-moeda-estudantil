package com.exemplo.app.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envia os e-mails publicados com ApplicationEventPublisher#publishEvent(Email).
 * Roda depois do commit (não avisa sobre uma operação desfeita) e em outra thread
 * (o usuário não espera o servidor de e-mail). Falha no envio só gera log.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.remetente}")
    private String remetente;

    @Async
    @TransactionalEventListener
    public void enviar(Email email) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email.para());
        mensagem.setSubject(email.assunto());
        mensagem.setText(email.texto());
        try {
            mailSender.send(mensagem);
        } catch (MailException e) {
            log.warn("Falha ao enviar e-mail para {} ({}): {}", email.para(), email.assunto(), e.getMessage());
        }
    }
}
