# Do Laravel ao Spring Boot — guia da API de Tarefas

> Guia de estudo ponta a ponta sobre este projeto: por que cada pasta existe, o que cada anotação faz de verdade, e como isso tudo se conecta — sempre comparando com o que já se conhece no Laravel.

Toda vez que aparecer um bloco `Laravel ⇄ Spring Boot`, é uma comparação direta entre o que você já conhece e o equivalente usado aqui. Leia isso não como "decoreba de sintaxe", mas como um mapa: os dois frameworks resolvem os mesmos problemas (rotear uma requisição, validar entrada, falar com o banco, tratar erro) com filosofias diferentes. Entender *essa* diferença é o que ajuda a responder bem numa entrevista.

## Sumário

1. [Visão geral do projeto e estrutura de pastas](#1-visão-geral-do-projeto-e-estrutura-de-pastas)
2. [A base de tudo: Inversão de Controle e Injeção de Dependência](#2-a-base-de-tudo-inversão-de-controle-e-injeção-de-dependência)
3. [O Model do Spring: a entidade `Tarefa`](#3-o-model-do-spring-a-entidade-tarefa)
4. [O Repository: falando com o banco sem escrever SQL](#4-o-repository-falando-com-o-banco-sem-escrever-sql)
5. [DTOs: o contrato entre a API e o mundo externo](#5-dtos-o-contrato-entre-a-api-e-o-mundo-externo)
6. [O Service: onde mora a regra de negócio](#6-o-service-onde-mora-a-regra-de-negócio)
7. [O Controller: a porta de entrada HTTP](#7-o-controller-a-porta-de-entrada-http)
8. [Validação: Bean Validation](#8-validação-bean-validation)
9. [Tratamento de exceções: um lugar só para todos os erros](#9-tratamento-de-exceções-um-lugar-só-para-todos-os-erros)
10. [O fluxo completo, ponta a ponta](#10-o-fluxo-completo-ponta-a-ponta)
11. [Configuração e o "autoconfigure" do Spring Boot](#11-configuração-e-o-autoconfigure-do-spring-boot)
12. [Documentação viva: Swagger / OpenAPI](#12-documentação-viva-swagger--openapi)
13. [Como estender sozinho: receita para uma nova feature](#13-como-estender-sozinho-receita-para-uma-nova-feature)
14. [Perguntas prováveis de entrevista](#14-perguntas-prováveis-de-entrevista)
15. [Cola rápida: Laravel ⇄ Spring Boot](#15-cola-rápida-laravel--spring-boot)

---

## 1. Visão geral do projeto e estrutura de pastas

O projeto é uma API REST de tarefas (CRUD completo) organizada em **arquitetura em camadas** (*layered architecture*): cada pasta é uma responsabilidade, e uma camada só conversa com a camada imediatamente abaixo dela. É o estilo de organização mais comum em projetos Spring Boot de porte pequeno/médio — o equivalente, em espírito, a manter Controllers, Models, Requests e Resources cada um na sua pasta no Laravel, só que aqui o Spring adiciona uma camada extra explícita (Service) entre o Controller e o acesso a dados.

```
com.pessoal.first_spring_app
├── controller/          → recebe HTTP, devolve HTTP
│   ├── HelloWorldController.java
│   └── TarefaController.java
├── service/             → regra de negócio
│   ├── TarefaService.java        (interface = "contrato")
│   └── TarefaServiceImpl.java    (implementação)
├── repository/          → acesso ao banco
│   └── TarefaRepository.java
├── model/               → o que é uma "Tarefa" (tabela)
│   └── Tarefa.java
├── dto/                 → formato dos dados que entram/saem pela API
│   ├── TarefaRequestDTO.java
│   ├── TarefaResponseDTO.java
│   └── ErrorResponseDTO.java
├── exception/           → erros customizados + tratamento global
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
├── config/              → configuração manual (beans)
│   └── OpenApiConfig.java
└── FirstSpringAppApplication.java    → ponto de entrada
```

**Laravel ⇄ Spring Boot**
- **Laravel:** `app/Http/Controllers`, `app/Models`, `app/Http/Requests`, `app/Http/Resources` — pastas separadas por *tipo de arquivo*, convenção de nomes resolve o resto.
- **Spring Boot:** mesma ideia, mas o Spring formaliza a camada de serviço (que no Laravel normalmente você cria por conta própria) e usa *pacotes Java* em vez de pastas soltas — pacote também define visibilidade de código.

Por que separar em pacotes por *camada* (controller/service/repository) e não por *funcionalidade* (ex.: um pacote `tarefa` com tudo dentro)? As duas abordagens são válidas — "package by layer" é mais didática e é a que a maioria dos tutoriais e vagas júnior esperam ver; "package by feature" escala melhor em projetos grandes. Para um projeto de estudo/entrevista, package-by-layer deixa mais óbvio "onde fica o quê", então foi a escolhida aqui.

## 2. A base de tudo: Inversão de Controle e Injeção de Dependência

Antes de entrar em cada arquivo, precisa entender uma peça que aparece em *todos* eles: o Spring mantém um **container** (o *Application Context*) que cria e guarda os objetos da sua aplicação — chamados de **beans**. Você não faz `new TarefaServiceImpl()` em lugar nenhum do código; você só declara "eu preciso de um `TarefaService`" e o Spring entrega a instância certa.

**Laravel ⇄ Spring Boot**
- **Laravel:** o **Service Container** faz a mesma coisa. Quando você digita um tipo no construtor de um Controller, o Laravel resolve automaticamente (*auto-wiring*). Bindings mais complexos você registra manualmente num `ServiceProvider` (`$this->app->bind(...)`).
- **Spring Boot:** o **ApplicationContext** faz o mesmo. Uma classe vira candidata a bean quando anotada com `@Component` (ou especializações: `@Service`, `@Repository`, `@RestController`). O Spring acha essas classes via *component scan* automático — não precisa registrar nada manualmente na maioria dos casos.

No projeto, isso aparece três vezes:
- `@RestController` no `TarefaController` — vira bean.
- `@Service` no `TarefaServiceImpl` — vira bean.
- O `TarefaRepository` nem precisa de anotação: por estender `JpaRepository`, o módulo Spring Data JPA já cria e registra a implementação sozinho.

A entrega da dependência acontece pelo **construtor**:

```java
// TarefaController.java
public TarefaController(TarefaService tarefaService) {
    this.tarefaService = tarefaService;
}
```

O Spring vê que o construtor pede um `TarefaService`, olha no container, encontra o único bean que implementa essa interface (`TarefaServiceImpl`) e injeta ali — sem você escrever uma linha de "fiação". Isso se chama **injeção via construtor**, e é a forma recomendada (em vez de `@Autowired` direto em um campo) por três motivos: o campo pode ser `final` (imutável), fica explícito no construtor tudo que a classe precisa pra existir, e testes unitários ficam triviais — `new TarefaServiceImpl(repositoryFalso)` sem precisar subir o Spring inteiro.

> **Por que uma interface, se só existe uma implementação?**
> `TarefaController` depende de `TarefaService` (a interface), nunca de `TarefaServiceImpl` diretamente. Isso é o princípio de **Inversão de Dependência**: quem decide a implementação concreta é o container, não quem consome. Na prática, isso permite trocar a implementação (ex.: uma versão "fake" em teste) sem tocar no controller — o análogo mais próximo no Laravel é o `bind()` de uma interface para uma implementação dentro de um Service Provider.

## 3. O Model do Spring: a entidade `Tarefa`

Toda tabela do banco que a aplicação manipula via JPA/Hibernate precisa de uma classe Java anotada com `@Entity` — é o equivalente direto de uma classe que estende `Model` no Eloquent.

```java
// model/Tarefa.java
@Entity
@Table(name = "tarefas")
@Getter @Setter @NoArgsConstructor
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

    @PrePersist
    void preencherCriadaEm() { this.criadaEm = LocalDateTime.now(); }
}
```

Linha a linha:
- **`@Entity`** — diz ao Hibernate "esta classe representa uma tabela".
- **`@Table(name = "tarefas")`** — nome exato da tabela. Sem essa anotação, o Hibernate infere um nome a partir da classe; ser explícito evita surpresa.
- **`@Id` + `@GeneratedValue`** — marca a chave primária e diz que ela é auto-incrementada pelo banco (`IDENTITY`).
- **`@Column`** — configura a coluna (obrigatória, tamanho, nome). Sem essa anotação, o Hibernate ainda cria a coluna, só que com os valores padrão.
- **`@PrePersist`** — um *hook de ciclo de vida*: roda automaticamente antes do `INSERT`.

**Laravel ⇄ Spring Boot**
- **Laravel:** `class Tarefa extends Model {}` — pronto. As colunas não existem como propriedades PHP: são resolvidas em tempo de execução via `__get`/`__set` a partir de um array de atributos carregado do banco.
- **Spring Boot:** cada coluna é um *campo Java real*, tipado e anotado. Mais verboso — mas o compilador garante que `tarefa.getTitulo()` sempre existe e sempre devolve uma `String`. Não tem "acesso mágico" por string solta.

> **Sobre o Lombok**
> `@Getter`/`@Setter`/`@NoArgsConstructor` não são do Spring — são do Lombok, uma biblioteca que gera esse código repetitivo em tempo de compilação (sem eles, cada campo exigiria escrever um getter e um setter à mão). É "açúcar sintático" puro, seguro de explicar como tal na entrevista.

## 4. O Repository: falando com o banco sem escrever SQL

```java
// repository/TarefaRepository.java
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findByConcluida(boolean concluida);
}
```

É *só uma interface* — sem implementação nenhuma escrita por você. Ao estender `JpaRepository<Tarefa, Long>` (a entidade e o tipo da chave primária), o Spring Data JPA gera uma implementação em tempo de execução (um *proxy*) com métodos prontos: `findAll()`, `findById()`, `save()`, `deleteById()`, `existsById()`, e dezenas de outros.

O método `findByConcluida` é o mais interessante para entender: **não tem corpo**. O Spring Data lê o *nome do método*, interpreta como uma query ("busque por Tarefa onde concluida = valor") e gera a consulta sozinho. Isso se chama **query derivada** (*derived query*).

**Laravel ⇄ Spring Boot**
- **Laravel:** `Tarefa::all()`, `Tarefa::find($id)`, `Tarefa::where('concluida', $v)->get()` — o próprio Eloquent Model já é o "repository" (query builder embutido na classe).
- **Spring Boot:** a responsabilidade fica numa interface à parte (não dentro da entidade). É mais uma camada, mas separa "o que é uma Tarefa" (a entidade) de "como buscar Tarefas" (o repository) — útil quando a lógica de acesso cresce.

> **Alternativa**
> Se uma query derivada do nome do método ficasse complexa demais para ler, a alternativa é anotar o método com `@Query("SELECT t FROM Tarefa t WHERE ...")` (JPQL, parecido com SQL) — equivalente a escrever uma query crua com `DB::table(...)` no Laravel quando o Eloquent não dá conta.

## 5. DTOs: o contrato entre a API e o mundo externo

DTO = *Data Transfer Object*. São classes que existem só para carregar dados através da rede — sem comportamento, sem regra de negócio. O projeto tem três:

```java
// dto/TarefaRequestDTO.java — o que o cliente pode enviar
public record TarefaRequestDTO(

        @NotBlank(message = "O titulo e obrigatorio")
        @Size(max = 120, message = "...")
        String titulo,

        @Size(max = 500, message = "...")
        String descricao

) {}
```

Note a palavra-chave **`record`** (Java 16+): é um tipo imutável — o compilador gera construtor, getters (`titulo()`, `descricao()`), `equals` e `hashCode` automaticamente. Ideal para DTOs, que são "só dados".

Por que não deixar o Controller receber a *entidade* `Tarefa` direto no corpo da requisição? Dois motivos práticos:
- **Mass assignment**: se o cliente pudesse mandar um JSON virando a entidade inteira, ele poderia enviar campos que não deveria controlar — por exemplo um `id` ou um `criadaEm` arbitrário. O DTO só declara os campos que o cliente tem permissão de definir.
- **Validação**: as anotações `@NotBlank`/`@Size` vivem no DTO, não na entidade — a entidade representa a tabela, o DTO representa "o que é aceitável entrar pela API". Nem sempre são a mesma coisa.

**Laravel ⇄ Spring Boot**
- **Laravel:** isso é exatamente o papel de um **Form Request** (`php artisan make:request`): a classe com o método `rules()` (`'titulo' => 'required|max:120'`) que valida antes do controller rodar — junto com `$fillable`/`$guarded` no Model, que resolve o mass assignment.
- **Spring Boot:** um único DTO acumula as duas responsabilidades (formato + regras de validação), porque no Spring o objeto de entrada já *é* tipado — não existe o equivalente de um array associativo solto para "vazar" campos extras.

```java
// dto/TarefaResponseDTO.java — o que a API devolve
public record TarefaResponseDTO(Long id, String titulo, String descricao,
                                  boolean concluida, LocalDateTime criadaEm) {

    public static TarefaResponseDTO fromEntity(Tarefa tarefa) {
        return new TarefaResponseDTO(tarefa.getId(), tarefa.getTitulo(),
                tarefa.getDescricao(), tarefa.isConcluida(), tarefa.getCriadaEm());
    }
}
```

O método estático `fromEntity` é um *mapper manual*: converte Entidade → DTO num único lugar, para não repetir esse mapeamento espalhado pelo service. É equivalente a um **API Resource** do Laravel (`TarefaResource extends JsonResource`) — ambos existem para desacoplar "o JSON que sai pela API" de "como a tabela é modelada por baixo".

O terceiro DTO, `ErrorResponseDTO`, padroniza o formato de qualquer erro (404, 400, 500) — volta na seção de exceções.

## 6. O Service: onde mora a regra de negócio

```java
// service/TarefaServiceImpl.java (trecho)
@Service
public class TarefaServiceImpl implements TarefaService {

    private final TarefaRepository tarefaRepository;

    public TarefaServiceImpl(TarefaRepository tarefaRepository) {
        this.tarefaRepository = tarefaRepository;
    }

    @Override
    public TarefaResponseDTO criar(TarefaRequestDTO dados) {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(dados.titulo());
        tarefa.setDescricao(dados.descricao());
        Tarefa salva = tarefaRepository.save(tarefa);
        return TarefaResponseDTO.fromEntity(salva);
    }

    private Tarefa buscarEntidadeOuFalhar(Long id) {
        return tarefaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa com id " + id + " nao encontrada"));
    }
}
```

O service recebe DTOs (já validados pelo controller), converte para/de entidade, chama o repository, e decide o que fazer quando algo "excepcional" acontece (como um id que não existe). Ele não sabe nada sobre HTTP — não devolve status code, não conhece `ResponseEntity`. Isso é proposital: o mesmo service poderia ser chamado por um job agendado, um evento, um teste, sem carregar bagagem de "protocolo web" junto.

`buscarEntidadeOuFalhar` é um pequeno helper privado que evita repetir `findById(...).orElseThrow(...)` em quatro métodos diferentes — o equivalente a um `findOrFail($id)` do Eloquent, que internamente já lança `ModelNotFoundException` se não achar nada.

**Laravel ⇄ Spring Boot**
- **Laravel:** muito projeto Laravel *não* tem uma camada de Service explícita — a regra de negócio mora direto no Controller, ou em Actions/Jobs isolados quando fica grande. Nada no framework força essa camada a existir.
- **Spring Boot:** por convenção da comunidade (não por exigência do framework), separar Controller de Service é quase padrão — sinaliza "aqui não tem regra de negócio, só HTTP" de um lado, e "aqui não tem HTTP, só regra de negócio" do outro.

## 7. O Controller: a porta de entrada HTTP

```java
// controller/TarefaController.java (trecho)
@RestController
@RequestMapping("/api/tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    @PostMapping
    public ResponseEntity<TarefaResponseDTO> criar(
            @Valid @RequestBody TarefaRequestDTO dados,
            UriComponentsBuilder uriBuilder) {

        TarefaResponseDTO criada = tarefaService.criar(dados);
        URI location = uriBuilder.path("/api/tarefas/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(location).body(criada);
    }
}
```

Anotação por anotação:
- **`@RestController`** = `@Controller` + `@ResponseBody`. Diz duas coisas ao Spring: "esta classe atende requisições HTTP" e "tudo que os métodos devolverem vira o corpo da resposta (serializado pra JSON automaticamente)".
- **`@RequestMapping("/api/tarefas")`** na classe — prefixo comum de todas as rotas abaixo.
- **`@GetMapping` / `@PostMapping` / `@PutMapping` / `@DeleteMapping`** — cada verbo HTTP mapeado explicitamente num método.
- **`@PathVariable`** — captura um pedaço da URL (`/{id}`) como parâmetro do método.
- **`@RequestBody`** — desserializa o JSON do corpo da requisição para o tipo do DTO.
- **`@Valid`** — dispara a validação Bean Validation nas anotações do DTO *antes* do corpo do método rodar.

`ResponseEntity<T>` dá controle total sobre a resposta HTTP (status, headers, corpo) — no exemplo, `ResponseEntity.created(location)` devolve **201** com um header `Location` apontando para o recurso recém-criado, uma boa prática REST que o Spring facilita e o Laravel não força.

**Laravel ⇄ Spring Boot**
- **Laravel:** `Route::apiResource('tarefas', TarefaController::class)` mapeia os 7 métodos (`index`, `store`, `show`, `update`, `destroy`...) por *convenção de nome de método* — uma linha de rota resolve tudo.
- **Spring Boot:** cada método precisa da sua anotação de verbo + caminho explícitos. Mais verboso, mas o roteamento fica *visível* olhando só pra classe — não precisa abrir um arquivo de rotas separado para saber o que existe.

## 8. Validação: Bean Validation

O padrão usado pela `spring-boot-starter-validation` chama-se **Bean Validation** (JSR 380) — as anotações (`@NotBlank`, `@Size`, e outras como `@Min`, `@Email`, `@Positive`) ficam nos campos do DTO, e o `@Valid` no parâmetro do controller ativa a checagem.

Se qualquer regra falhar, o Spring lança uma `MethodArgumentNotValidException` — o método do controller **nunca chega a executar**. É pega pelo `GlobalExceptionHandler` (próxima seção) e vira uma resposta 400 padronizada, com a lista de mensagens de erro.

**Laravel ⇄ Spring Boot**
- **Laravel:** um Form Request com `rules()` retornando um array de strings (`'required|max:120'`) — a validação roda automaticamente antes do controller, e uma falha lança `ValidationException` (422 por padrão).
- **Spring Boot:** as regras vivem como anotações no próprio DTO (não numa classe separada de "regras"), e você decide o status HTTP do erro no seu próprio `@ExceptionHandler` — no projeto, 400.

## 9. Tratamento de exceções: um lugar só para todos os erros

```java
// exception/GlobalExceptionHandler.java (trecho)
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> tratarRecursoNaoEncontrado(ResourceNotFoundException ex) {
        ErrorResponseDTO erro = ErrorResponseDTO.of(404, "Recurso nao encontrado", ex.getMessage(), List.of());
        return ResponseEntity.status(404).body(erro);
    }
}
```

**`@RestControllerAdvice`** transforma essa classe num interceptador global: toda exceção lançada em *qualquer* controller da aplicação passa por aqui antes de virar resposta HTTP — sem precisar de `try/catch` espalhado pelos controllers. Cada método anotado com `@ExceptionHandler(TipoDaExcecao.class)` trata um tipo específico.

O projeto tem três níveis:
1. `ResourceNotFoundException` (criada à mão, `extends RuntimeException`) → 404.
2. `MethodArgumentNotValidException` (lançada automaticamente pelo `@Valid`) → 400, com a lista de mensagens de cada campo inválido.
3. `Exception` (qualquer coisa não prevista) → 500 genérico, sem vazar stacktrace pro cliente.

**Laravel ⇄ Spring Boot**
- **Laravel:** o método `render()` (ou `renderable()`) dentro de `app/Exceptions/Handler.php` — mesmo papel: centralizar a conversão de exceção para resposta HTTP num único lugar do projeto.
- **Spring Boot:** em vez de um arquivo fixo com vários `if instanceof`, cada tipo de erro ganha seu próprio método anotado — mais fácil de escanear visualmente "quais erros esta API trata e como".

## 10. O fluxo completo, ponta a ponta

Juntando tudo: o que acontece, na ordem exata, quando alguém faz `POST /api/tarefas` com um título válido.

```
Cliente (envia JSON)
  → DispatcherServlet (roteia p/ o controller certo)
  → @Valid (valida o DTO)
  → Controller (chama o Service)
  → Service (DTO → Entidade)
  → Repository (INSERT via Hibernate)
  → H2 (grava e devolve o id)
  → Service (Entidade → DTO)
  → Controller (201 + Location)
  → Cliente (recebe o JSON criado)
```

E o caminho "torto", quando o título vem em branco:

```
Cliente (envia título vazio)
  → @Valid (falha)
  → MethodArgumentNotValidException (é lançada)
  → GlobalExceptionHandler (intercepta)
  → Cliente (recebe 400 + mensagem)
```

Repare que, no segundo fluxo, **o Controller nunca chega a executar seu corpo** — a validação barra antes. E o `Service` e o `Repository` nunca ficam sabendo que a requisição existiu. Cada camada só é acionada se a anterior deixar passar.

## 11. Configuração e o "autoconfigure" do Spring Boot

Três arquivos fecham o quadro de configuração do projeto:

### pom.xml — as dependências

Cada `<dependency>` adiciona não só uma biblioteca, mas um pedaço de comportamento "mágico" que o Spring Boot ativa sozinho ao detectar aquele jar no classpath — isso se chama **auto-configuration**. Ao adicionar `spring-boot-starter-data-jpa` + o driver do H2, o Spring Boot, sem nenhum código seu, já: cria a conexão com o banco, configura o Hibernate, e prepara tudo para os `@Entity`/`@Repository` funcionarem.

**Laravel ⇄ Spring Boot**
- **Laravel:** `composer require` instala a biblioteca, mas você geralmente ainda publica um `config/*.php` e ajusta o `.env` manualmente.
- **Spring Boot:** adicionar a dependência já é (quase) suficiente — o Boot inspeciona o que está no classpath e configura sensatamente sozinho. Você só sobrescreve o que quer mudar do padrão, no `application.properties`.

### application.properties — o `.env` do Spring

Guarda URL do banco, credenciais, e ligar/desligar recursos (como o console web do H2). É o análogo direto do `.env` do Laravel, com uma diferença: aqui não é usado para segredo em produção (isso vai em variáveis de ambiente ou *profiles* — um recurso do Spring para ter configurações diferentes por ambiente, parecido com ter um `.env.production` separado).

### OpenApiConfig.java — um bean configurado manualmente

```java
// config/OpenApiConfig.java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("API de Tarefas")
                .version("v1"));
    }
}
```

**`@Configuration`** marca a classe como fonte de beans *manuais* (em vez de descobertos por component scan). Cada método `@Bean` produz um objeto que o Spring guarda no container. É o equivalente mais próximo a um **Service Provider** do Laravel — a diferença é que aqui cada "registro" é só um método anotado, sem herdar de nenhuma classe base.

## 12. Documentação viva: Swagger / OpenAPI

A dependência `springdoc-openapi-starter-webmvc-ui` varre todos os `@RestController` da aplicação em tempo de execução e gera automaticamente: (1) um documento OpenAPI (JSON, em `/v3/api-docs`) descrevendo cada endpoint, parâmetro e schema; (2) uma interface visual (Swagger UI, em `/swagger-ui/index.html`) que lê esse documento e monta uma tela interativa, com botão "Try it out" em cada rota.

As anotações `@Tag` (na classe) e `@Operation(summary = "...")` (em cada método) só adicionam texto descritivo nessa tela — não mudam nenhum comportamento da API. O importante para a entrevista: **a documentação nunca fica desatualizada**, porque não é escrita à parte — ela é derivada do próprio código (assinatura dos métodos + tipos dos DTOs).

**Laravel ⇄ Spring Boot**
- **Laravel:** o equivalente mais comum é manter uma *collection* do Postman/Insomnia à mão, ou usar um pacote como `l5-swagger` — normalmente exige anotações extras (`@OA\...`) espalhadas pelos controllers, de forma parecida.
- **Spring Boot:** o springdoc já infere boa parte da documentação sozinho a partir das anotações de rota que já existiam (`@GetMapping`, etc.) — as anotações extras (`@Tag`/`@Operation`) são só enriquecimento opcional.

## 13. Como estender sozinho: receita para uma nova feature

Se pedirem para adicionar, por exemplo, um recurso de **Categoria**, o caminho segue exatamente o mesmo padrão do `Tarefa`, nesta ordem:

1. **Model** — criar `model/Categoria.java` com `@Entity`, campos e `@Id`.
2. **Repository** — `interface CategoriaRepository extends JpaRepository<Categoria, Long>`.
3. **DTOs** — `CategoriaRequestDTO` (com validações) e `CategoriaResponseDTO` (com `fromEntity`).
4. **Service** — interface `CategoriaService` + `CategoriaServiceImpl` com `@Service`, injeção do repository via construtor.
5. **Controller** — `CategoriaController` com `@RestController` + `@RequestMapping("/api/categorias")`, os cinco métodos CRUD.
6. Reaproveitar o `GlobalExceptionHandler` que já existe — não precisa duplicar nada de tratamento de erro.

Se quisesse ligar Categoria a Tarefa (uma tarefa pertence a uma categoria), adicionaria no `Tarefa.java`:

```java
// model/Tarefa.java (hipotético)
@ManyToOne
@JoinColumn(name = "categoria_id")
private Categoria categoria;
```

**Laravel ⇄ Spring Boot**
- **Laravel:** `public function categoria() { return $this->belongsTo(Categoria::class); }` — um método na classe do Model.
- **Spring Boot:** `@ManyToOne` num campo — a "direção" do relacionamento (quem tem a chave estrangeira) é declarada ali mesmo, e o Hibernate cria a coluna `categoria_id` sozinho (por causa do `ddl-auto=update`).

## 14. Perguntas prováveis de entrevista

<details>
<summary><strong>Por que separar Controller, Service e Repository em vez de tudo no Controller?</strong></summary>

Cada camada tem uma única responsabilidade (Single Responsibility): Controller só entende HTTP, Service só entende regra de negócio, Repository só entende acesso a dados. Isso facilita testar cada parte isoladamente (mockar o Service num teste de Controller, mockar o Repository num teste de Service) e evita o anti-padrão do "fat controller" — comum tanto em Spring quanto em Laravel quando a separação não é seguida.
</details>

<details>
<summary><strong>Por que DTOs em vez de expor a entidade JPA direto na API?</strong></summary>

Duas razões: segurança (evita mass assignment — o cliente só define o que o DTO de entrada permite) e desacoplamento (o formato exposto pela API pode evoluir independentemente do esquema da tabela; a entidade pode até ganhar novos relacionamentos sem quebrar o contrato JSON).
</details>

<details>
<summary><strong>O que é injeção de dependência e por que ela importa?</strong></summary>

É o container do Spring (IoC Container) criando e entregando as dependências de uma classe, em vez da própria classe instanciá-las com `new`. Isso inverte o controle — daí "Inversão de Controle" — e permite trocar implementações, mockar em testes e configurar o "grafo de objetos" da aplicação em um lugar central, não espalhado pelo código.
</details>

<details>
<summary><strong>Por que injeção via construtor em vez de <code>@Autowired</code> em campo?</strong></summary>

Permite que o campo seja `final` (imutável e sempre inicializado), deixa explícitas no construtor todas as dependências da classe, e facilita testes unitários puros — dá pra instanciar a classe manualmente com dependências falsas, sem subir o contexto do Spring.
</details>

<details>
<summary><strong>O que faz o <code>@RestControllerAdvice</code>?</strong></summary>

Intercepta exceções lançadas por qualquer controller da aplicação e as centraliza em um único lugar, convertendo cada tipo de exceção numa resposta HTTP padronizada — em vez de espalhar `try/catch` em cada endpoint.
</details>

<details>
<summary><strong>O que é <code>ddl-auto=update</code> e por que não usar em produção?</strong></summary>

É o Hibernate criando/alterando as tabelas automaticamente com base nas entidades `@Entity` — ótimo para prototipar/estudar, perigoso em produção porque pode alterar uma tabela real de forma imprevisível (perder dados, mudar tipos). Em produção o normal é usar uma ferramenta de migração versionada (ex.: Flyway/Liquibase no mundo Spring — o equivalente às migrations do Laravel) e deixar `ddl-auto=validate` ou `none`.
</details>

<details>
<summary><strong>Qual a diferença entre <code>@Component</code>, <code>@Service</code> e <code>@Repository</code>?</strong></summary>

Funcionalmente, todas registram a classe como bean no container — `@Service` e `@Repository` são especializações semânticas de `@Component` (dizem *a intenção* da classe) e, no caso de `@Repository`, também ativam tradução automática de exceções específicas de banco para exceções do Spring.
</details>

## 15. Cola rápida: Laravel ⇄ Spring Boot

| Laravel | Spring Boot | Papel |
|---|---|---|
| Service Container | ApplicationContext | Cria e injeta dependências |
| `ServiceProvider::bind()` | `@Component` / `@Service` / `@Bean` | Registrar algo no container |
| Eloquent Model | `@Entity` (JPA) | Representa uma tabela |
| Eloquent query builder | Repository (`JpaRepository`) | Acesso a dados |
| Form Request | DTO + `@Valid` | Validar entrada |
| API Resource | DTO de resposta | Formatar saída |
| `Handler::render()` | `@RestControllerAdvice` | Tratar exceções globalmente |
| `Route::apiResource()` | `@GetMapping`/`@PostMapping`/... | Roteamento |
| `.env` | `application.properties` | Configuração |
| Migrations | Flyway/Liquibase (ou `ddl-auto` em estudo) | Versionar esquema do banco |
| `php artisan serve` | `mvnw spring-boot:run` | Rodar a aplicação |

---

*Guia gerado a partir do próprio código do projeto `first-spring-app` — toda anotação e trecho mostrado aqui existe de verdade nos arquivos, sem simplificação inventada.*
