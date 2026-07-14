package com.pessoal.first_spring_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Endpoint minimo, mantido apenas como "smoke test" (confirma que a aplicacao subiu).
 *
 * RestController = Controller + ResponseBody: toda String/objeto retornado pelos
 * metodos vira o corpo da resposta HTTP (JSON, se for um objeto). No Laravel isso e
 * automatico em qualquer controller: um `return response()->json($algo)` ou ate um
 * `return $algo` em uma rota de API ja serializa para JSON, sem anotacao nenhuma.
 *
 * RequestMapping na classe define o prefixo comum das rotas abaixo, similar a um
 * Route::prefix('hello-world')->group(...) no Laravel.
 */
@RestController
@RequestMapping("/hello-world")
public class HelloWorldController {

    @GetMapping
    public String helloWorld() {
        return "Hello World!";
    }
}
