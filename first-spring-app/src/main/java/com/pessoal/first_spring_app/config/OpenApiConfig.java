package com.pessoal.first_spring_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/*
 * @Configuration marca a classe como fonte de "beans" configurados manualmente
 * (em vez de descobertos por component scan como @Service/@Controller). Cada
 * metodo @Bean produz um objeto que o Spring guarda no container e pode
 * injetar em qualquer outro lugar. E o equivalente mais proximo a um
 * Service Provider do Laravel (o metodo register()/boot() que registra
 * bindings manuais no Container) - a diferenca e que aqui cada "binding" e so
 * um metodo anotado, sem precisar herdar de nenhuma classe base.
 *
 * Este bean especifico so preenche os metadados (titulo/descricao/versao) que
 * aparecem no topo da tela do Swagger UI - nao afeta nenhum endpoint.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("API de Tarefas")
                        .description("Projeto de estudo em Spring Boot (arquitetura em camadas: "
                                + "Controller, Service, Repository, DTOs e tratamento global de excecoes).")
                        .version("v1")
        );
    }
}
