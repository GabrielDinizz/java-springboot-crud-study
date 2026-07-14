package com.pessoal.first_spring_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pessoal.first_spring_app.model.Tarefa;

/*
 * Repository = a camada de acesso a dados, equivalente ao proprio Eloquent
 * Model quando voce chama Tarefa::all(), Tarefa::find($id), etc.
 *
 * A diferenca e que no Spring essa responsabilidade fica numa interface a
 * parte (nao dentro da entidade). Ao estender JpaRepository<Tarefa, Long>
 * (Long = tipo da chave primaria), ja ganhamos de graca: findAll(), findById(),
 * save(), deleteById(), existsById(), entre outros - sem escrever nenhuma
 * implementacao. O Spring gera a implementacao em tempo de execucao (proxy).
 *
 * Se precisar de uma consulta customizada, basta declarar o metodo pelo nome
 * (Spring Data "deriva" a query a partir da assinatura) - o comentario abaixo
 * mostra o equivalente a um scope/where do Eloquent.
 */
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    // Exemplo de query derivada do nome do metodo, sem escrever SQL/JPQL:
    // equivalente a Tarefa::where('concluida', $valor)->get() no Eloquent.
    List<Tarefa> findByConcluida(boolean concluida);
}
