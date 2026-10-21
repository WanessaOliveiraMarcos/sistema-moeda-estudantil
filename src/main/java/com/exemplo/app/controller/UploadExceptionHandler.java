package com.exemplo.app.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {

    // o arquivo é recusado antes de chegar ao controller; volta para a listagem com aviso
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String arquivoGrande(HttpServletRequest request, RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", "A foto deve ter no máximo 5 MB");
        return "redirect:" + caminhoOriginal(request);
    }

    // a falha costuma ser tratada já no encaminhamento para /error, então usa a URI que o navegador pediu
    private String caminhoOriginal(HttpServletRequest request) {
        String uri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (uri == null) {
            uri = request.getRequestURI();
        }
        // remove o context path e evita "//host" (redirecionamento para outro site)
        return uri.substring(request.getContextPath().length()).replaceFirst("^/+", "/");
    }
}
