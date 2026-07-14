package com.pessoal.first_spring_app.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.pessoal.first_spring_app.dto.TarefaRequestDTO;
import com.pessoal.first_spring_app.dto.TarefaResponseDTO;
import com.pessoal.first_spring_app.service.TarefaService;

/*
 * Controller = camada HTTP pura: recebe a requisicao, delega pro service e
 * devolve a resposta. Nao tem regra de negocio nem acesso a repository aqui -
 * isso e o que separa este projeto de um Controller "gordo" (fat controller),
 * anti-padrao comum tanto em Spring quanto em Laravel.
 *
 * Semelhante a um "api resource controller" do Laravel
 * (Route::apiResource('tarefas', TarefaController::class)), so que aqui cada
 * verbo HTTP e mapeado explicitamente com @GetMapping/@PostMapping/etc. em vez
 * de resolvido por convenção de nomes de metodo (index/store/show/update/destroy).
 *
 * @Valid no parametro dispara a validacao do Bean Validation (as anotacoes do
 * TarefaRequestDTO). Se falhar, uma MethodArgumentNotValidException e lancada
 * e tratada pelo GlobalExceptionHandler - equivalente a como um Form Request
 * do Laravel aborta a requisicao antes de chegar no corpo do metodo.
 *
 * @Tag/@Operation (do springdoc-openapi) so adicionam texto descritivo na tela
 * do Swagger UI (/swagger-ui.html) - nao mudam nenhum comportamento da API.
 * O springdoc le as anotacoes @GetMapping/@PostMapping/etc. e os tipos dos
 * DTOs automaticamente para montar a documentacao; essas duas anotacoes so
 * enriquecem o texto exibido.
 */
@Tag(name = "Tarefas", description = "CRUD de tarefas")
@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @Operation(summary = "Lista todas as tarefas")
    // GET /api/tarefas -> equivalente ao "index" de um resource controller
    @GetMapping
    public ResponseEntity<List<TarefaResponseDTO>> listar() {
        return ResponseEntity.ok(tarefaService.listarTodas());
    }

    @Operation(summary = "Busca uma tarefa pelo id")
    // GET /api/tarefas/{id} -> equivalente ao "show"
    @GetMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tarefaService.buscarPorId(id));
    }

    @Operation(summary = "Cria uma nova tarefa")
    // POST /api/tarefas -> equivalente ao "store"
    @PostMapping
    public ResponseEntity<TarefaResponseDTO> criar(
            @Valid @RequestBody TarefaRequestDTO dados,
            UriComponentsBuilder uriBuilder) {

        TarefaResponseDTO tarefaCriada = tarefaService.criar(dados);

        // 201 Created + header "Location" apontando para o recurso criado:
        // boa pratica REST que o Laravel nao forca, mas que o Spring facilita.
        URI location = uriBuilder.path("/api/tarefas/{id}")
                .buildAndExpand(tarefaCriada.id())
                .toUri();

        return ResponseEntity.created(location).body(tarefaCriada);
    }

    @Operation(summary = "Atualiza uma tarefa existente")
    // PUT /api/tarefas/{id} -> equivalente ao "update"
    @PutMapping("/{id}")
    public ResponseEntity<TarefaResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TarefaRequestDTO dados) {

        return ResponseEntity.ok(tarefaService.atualizar(id, dados));
    }

    @Operation(summary = "Exclui uma tarefa")
    // DELETE /api/tarefas/{id} -> equivalente ao "destroy"
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        tarefaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
