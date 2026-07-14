package com.pessoal.first_spring_app.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.pessoal.first_spring_app.dto.ErrorResponseDTO;

/*
 * @RestControllerAdvice = um interceptador global de excecoes para todos os
 * controllers da aplicacao. Em vez de cada controller ter blocos try/catch
 * espalhados, as excecoes "sobem" ate aqui e sao convertidas no formato de
 * resposta padrao (ErrorResponseDTO).
 *
 * Equivalente direto no Laravel: o metodo `render()` (ou os `register()` com
 * `renderable()`) do app/Exceptions/Handler.php, que tambem centraliza a
 * conversao de excecao -> resposta HTTP.
 *
 * Cada metodo abaixo trata um tipo de excecao especifico e decide o status
 * HTTP correto - essa e a "ponte" entre erros de negocio (Java puro, sem saber
 * o que e HTTP) e o protocolo HTTP em si.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404: recurso nao encontrado (ex.: TarefaController.buscarPorId com id invalido)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> tratarRecursoNaoEncontrado(ResourceNotFoundException ex) {
        ErrorResponseDTO erro = ErrorResponseDTO.of(
                HttpStatus.NOT_FOUND.value(),
                "Recurso nao encontrado",
                ex.getMessage(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    // 400: corpo da requisicao nao passou nas validacoes do @Valid (Bean Validation).
    // Equivalente ao 422 automatico que o Laravel devolve quando um Form Request falha.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> tratarValidacaoInvalida(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        ErrorResponseDTO erro = ErrorResponseDTO.of(
                HttpStatus.BAD_REQUEST.value(),
                "Dados invalidos",
                "Um ou mais campos nao passaram na validacao",
                detalhes
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    // Fallback: qualquer excecao nao tratada especificamente vira 500, mas ainda
    // no formato padrao de erro (nunca expondo stacktrace cru pro cliente).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> tratarErroInesperado(Exception ex) {
        ErrorResponseDTO erro = ErrorResponseDTO.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                List.of()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
