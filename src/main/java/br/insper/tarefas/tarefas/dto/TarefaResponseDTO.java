package br.insper.tarefas.tarefas.dto;

import br.insper.tarefas.tarefas.Tarefa;

import java.time.LocalDateTime;

public record TarefaResponseDTO(
        Long id,
        String titulo,
        Integer prioridade,
        Boolean concluida,
        LocalDateTime dataCriacao,
        String emailResponsavel
) {
    public static TarefaResponseDTO from(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getTitulo(),
                tarefa.getPrioridade(),
                tarefa.getConcluida(),
                tarefa.getDataCriacao(),
                tarefa.getEmailResponsavel()
        );
    }
}
