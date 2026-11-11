package br.insper.tarefas.tarefas;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "tarefas")
public class Tarefa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descricao;
    private Integer prioridade;
    private String emailResponsavel;
    private Boolean concluida;
    private LocalDateTime dataCriacao;

    public Tarefa() {}

    public Tarefa(String titulo, String descricao, Integer prioridade, String emailResponsavel) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.emailResponsavel = emailResponsavel;
        this.concluida = false;
        this.dataCriacao = LocalDateTime.now();
    }

    public String getEmailResponsavel() {
        return emailResponsavel;
    }
}
