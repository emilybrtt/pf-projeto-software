package br.insper.tarefas.tarefas.dto;

public record CreateTarefaRequestDTO(
        String titulo,
        String descricao,
        Integer prioridade
) {}
