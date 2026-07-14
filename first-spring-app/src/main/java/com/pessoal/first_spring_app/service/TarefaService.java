package com.pessoal.first_spring_app.service;

import java.util.List;

import com.pessoal.first_spring_app.dto.TarefaRequestDTO;
import com.pessoal.first_spring_app.dto.TarefaResponseDTO;

/*
 * Por que uma interface para o service, se so existe uma implementacao?
 *
 * 1) O controller depende da abstracao (TarefaService), nao da classe concreta.
 *    Isso e o principio de Inversao de Dependencia: quem "decide" qual
 *    implementacao entra em cena e o Spring (via @Service + injecao pelo tipo),
 *    nao o controller.
 * 2) Facilita testes: em um teste de controller, e possivel mockar essa
 *    interface (@MockBean) sem tocar no banco.
 * 3) E o analogo mais proximo do "bind" de uma interface para uma implementacao
 *    num Service Provider do Laravel (`$this->app->bind(TarefaService::class,
 *    TarefaServiceImpl::class)`), so que o Spring faz esse "bind" por conveção:
 *    ele acha a unica classe @Service que implementa a interface e injeta ela.
 */
public interface TarefaService {

    List<TarefaResponseDTO> listarTodas();

    TarefaResponseDTO buscarPorId(Long id);

    TarefaResponseDTO criar(TarefaRequestDTO dados);

    TarefaResponseDTO atualizar(Long id, TarefaRequestDTO dados);

    void excluir(Long id);
}
