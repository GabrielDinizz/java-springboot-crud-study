package com.pessoal.first_spring_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/*
 * DTO (Data Transfer Object) de entrada: representa exatamente o que o cliente
 * pode enviar no corpo da requisicao - nada mais, nada menos.
 *
 * Por que nao receber a entidade Tarefa direto no controller?
 * - Mass assignment: se o controller aceitasse a entidade inteira, o cliente
 *   poderia mandar campos que nao deveria controlar (ex.: "id" ou "criadaEm").
 *   No Laravel isso e resolvido com $fillable/$guarded no Model ou validando
 *   os dados de um Form Request; aqui o DTO cumpre esse papel: so existe campo
 *   para o que o cliente tem permissao de definir.
 * - As anotacoes de validacao (@NotBlank, @Size) sao o equivalente as regras de
 *   um Form Request (`'titulo' => 'required|max:120'`). Quem dispara a validacao
 *   e o @Valid no metodo do controller.
 *
 * Usar "record" (Java 16+) em vez de classe comum: e um tipo imutavel, gera
 * construtor/getters/equals/hashCode automaticamente. Ideal para DTOs, que so
 * carregam dados e nao tem comportamento.
 */
public record TarefaRequestDTO(

        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres")
        String titulo,

        @Size(max = 500, message = "A descricao deve ter no maximo 500 caracteres")
        String descricao

) {
}
