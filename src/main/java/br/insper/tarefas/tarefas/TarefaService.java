package br.insper.tarefas.tarefas;

import br.insper.tarefas.tarefas.dto.CreateTarefaRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository repository;

    public TarefaService(TarefaRepository repository) {
        this.repository = repository;
    }

    public Tarefa create(CreateTarefaRequestDTO request, String emailResponsavel) {
        if (request.prioridade() == null || request.prioridade() < 1 || request.prioridade() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "prioridade deve estar entre 1 e 5");
        }

        Tarefa tarefa = new Tarefa(request.titulo(), request.descricao(), request.prioridade(), request.emailResponsavel());
        return repository.save(tarefa);
    }

    public List<Tarefa> list() {
        return repository.findAll();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada");
        }
        repository.deleteById(id);
    }
}
