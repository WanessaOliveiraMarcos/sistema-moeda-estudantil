package com.exemplo.app.email;

/** E-mail a enviar. Publicado como evento e enviado só depois que a transação for confirmada. */
public record Email(String para, String assunto, String texto) {
}
