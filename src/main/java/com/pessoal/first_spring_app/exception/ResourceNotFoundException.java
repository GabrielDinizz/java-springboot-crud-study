package com.pessoal.first_spring_app.exception;

/*
 * Exception customizada e "de negocio": lancada quando um recurso pedido
 * (ex.: Tarefa com id=99) nao existe. RuntimeException porque nao faz sentido
 * forcar todo chamador a declarar "throws" (o Java tem excecoes checked e
 * unchecked; unchecked = mais parecido com o modelo de excecoes do PHP).
 *
 * Quem transforma isso num HTTP 404 e o GlobalExceptionHandler - a excecao em
 * si so carrega a mensagem, sem saber nada sobre HTTP. Isso mantem o service
 * desacoplado da camada web (o mesmo service poderia ser chamado por um job,
 * um evento, um teste, etc., sem carregar status HTTP junto).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }
}
