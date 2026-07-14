package com.pessoal.first_spring_app.dto;

import java.time.LocalDateTime;

import com.pessoal.first_spring_app.model.Tarefa;

/*
 * DTO de saida: controla exatamente o que volta para o cliente.
 *
 * Por que nao devolver a entidade Tarefa direto (com @RestController serializando
 * ela pra JSON)? Porque a entidade e o formato de dados usado pelo JPA, e tudo o
 * que a entidade tiver (inclusive relacionamentos, campos internos, etc.) vazaria
 * na API. O DTO de resposta e um "contrato" estavel com o cliente, independente
 * de como a tabela e modelada por baixo.
 *
 * Equivalente no Laravel: um API Resource (`TarefaResource extends JsonResource`),
 * que tambem existe justamente para desacoplar o JSON exposto do Model/tabela.
 */
public record TarefaResponseDTO(
        Long id,
        String titulo,
        String descricao,
        boolean concluida,
        LocalDateTime criadaEm
) {

    // Metodo de fabrica: converte Entidade -> DTO num unico lugar (evita repetir
    // esse mapeamento em cada metodo do service).
    public static TarefaResponseDTO fromEntity(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.isConcluida(),
                tarefa.getCriadaEm()
        );
    }
}
