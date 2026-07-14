package com.pessoal.first_spring_app.dto;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Formato unico de resposta para qualquer erro da API (404, 400, 500...).
 * Ter um "contrato" fixo de erro facilita muito a vida de quem consome a API
 * (o frontend sempre sabe onde achar "mensagem" e "erros").
 *
 * Equivalente no Laravel: o JSON padronizado que o
 * app/Exceptions/Handler.php (ou o Handler::render) devolve, por exemplo o
 * formato que o ValidationException::class gera automaticamente com "message"
 * e "errors".
 */
public record ErrorResponseDTO(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        List<String> detalhes
) {
    public static ErrorResponseDTO of(int status, String erro, String mensagem, List<String> detalhes) {
        return new ErrorResponseDTO(LocalDateTime.now(), status, erro, mensagem, detalhes);
    }
}
