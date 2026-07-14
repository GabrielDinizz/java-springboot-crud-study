package com.pessoal.first_spring_app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pessoal.first_spring_app.dto.TarefaRequestDTO;
import com.pessoal.first_spring_app.dto.TarefaResponseDTO;
import com.pessoal.first_spring_app.exception.ResourceNotFoundException;
import com.pessoal.first_spring_app.model.Tarefa;
import com.pessoal.first_spring_app.repository.TarefaRepository;

/*
 * @Service marca a classe como um "bean" gerenciado pelo Spring - ela entra no
 * container de injecao de dependencia. E o analogo a registrar uma classe no
 * Service Container do Laravel, so que aqui e automatico via component scan
 * (o Spring varre os pacotes procurando por @Service/@Component/@Repository/etc.,
 * nao e preciso registrar manualmente em lugar nenhum).
 *
 * Aqui mora a regra de negocio: validacao ja foi feita no controller (via @Valid
 * no DTO), entao o service pode confiar que os dados chegam validos e focar em
 * orquestrar o repository + tratar regras (ex.: "nao existe -> lanca excecao").
 *
 * Injecao via construtor (em vez de @Autowired em campo): permite que o campo
 * seja "final" (imutavel) e deixa explicito, no construtor, tudo que a classe
 * precisa para funcionar - o que tambem facilita testes unitarios (basta
 * `new TarefaServiceImpl(repositoryFalso)`). Equivale a injecao via construtor
 * de uma classe registrada no Container do Laravel.
 */
@Service
public class TarefaServiceImpl implements TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaServiceImpl(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Override
    public List<TarefaResponseDTO> listarTodas() {
        return tarefaRepository.findAll()
                .stream()
                .map(TarefaResponseDTO::fromEntity)
                .toList();
    }

    @Override
    public TarefaResponseDTO buscarPorId(Long id) {
        Tarefa tarefa = buscarEntidadeOuFalhar(id);
        return TarefaResponseDTO.fromEntity(tarefa);
    }

    @Override
    public TarefaResponseDTO criar(TarefaRequestDTO dados) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dados.titulo());
        tarefa.setDescricao(dados.descricao());

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);
        return TarefaResponseDTO.fromEntity(tarefaSalva);
    }

    @Override
    public TarefaResponseDTO atualizar(Long id, TarefaRequestDTO dados) {
        Tarefa tarefa = buscarEntidadeOuFalhar(id);
        tarefa.setTitulo(dados.titulo());
        tarefa.setDescricao(dados.descricao());

        Tarefa tarefaAtualizada = tarefaRepository.save(tarefa);
        return TarefaResponseDTO.fromEntity(tarefaAtualizada);
    }

    @Override
    public void excluir(Long id) {
        Tarefa tarefa = buscarEntidadeOuFalhar(id);
        tarefaRepository.delete(tarefa);
    }

    // Metodo privado de apoio: evita repetir "findById + orElseThrow" em cada
    // metodo publico acima (equivalente a um findOrFail($id) do Eloquent, que
    // ja lanca ModelNotFoundException internamente).
    private Tarefa buscarEntidadeOuFalhar(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa com id " + id + " nao encontrada"));
    }
}
