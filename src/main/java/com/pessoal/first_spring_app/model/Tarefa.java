package com.pessoal.first_spring_app.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * Entidade JPA = o equivalente ao Model do Eloquent.
 *
 * Diferencas principais para quem vem do Laravel:
 * - No Eloquent, uma classe "Tarefa extends Model" ja basta: os campos da tabela nao
 *   aparecem como propriedades no PHP, sao resolvidos em tempo de execucao via magic
 *   methods (__get/__set) a partir do array de atributos.
 * - No JPA, a entidade e uma classe Java "de verdade": cada coluna precisa de um campo
 *   tipado e anotado. E mais verboso, mas o compilador garante que os nomes/tipos batem
 *   com o resto do codigo (sem "toArray()" nem acesso via string solta tipo $tarefa->titulo).
 * - @Table define o nome da tabela (equivalente ao protected $table = 'tarefas', so que
 *   aqui e explicito em vez de inferido por convencao de plural).
 *
 * Uso o Lombok (@Getter/@Setter) para nao escrever getters/setters manualmente -
 * e o unico "acucar sintatico" aqui; sem ele cada campo exigiria ~6 linhas de boilerplate.
 */
@Entity
@Table(name = "tarefas")
@Getter
@Setter
@NoArgsConstructor
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String titulo;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false)
    private boolean concluida = false;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    // Equivalente a um hook "creating" de Model no Eloquent: roda antes do INSERT.
    @PrePersist
    void preencherCriadaEm() {
        this.criadaEm = LocalDateTime.now();
    }
}
