package br.insper.tarefas.tarefas;

import br.insper.tarefas.tarefas.dto.CreateTarefaRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TarefaServiceTests {

    @InjectMocks
    private TarefaService tarefaService;

    @Mock
    private TarefaRepository tarefaRepository;

    @Test
    public void test_shouldCreateTarefaWhenPrioridadeIsValid() {
        CreateTarefaRequestDTO dto = new CreateTarefaRequestDTO("Revisar PR", "Fazer a revisão", 3);
        Tarefa tarefa = new Tarefa(dto.titulo(), dto.descricao(), dto.prioridade(), "resp@insper.edu.br");

        Mockito.when(tarefaRepository.save(Mockito.any()))
                .thenReturn(tarefa);

        Tarefa response = tarefaService.create(dto, "resp@insper.edu.br");

        Assertions.assertEquals(3, response.getPrioridade());
        Assertions.assertFalse(response.getConcluida());
    }

    @Test
    public void test_shouldThrowBadRequestWhenPrioridadeIsAboveFive() {
        CreateTarefaRequestDTO dto = new CreateTarefaRequestDTO("Revisar PR", "Fazer revisão",6);

        ResponseStatusException exception = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.create(dto, "resp@insper.edu.br"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void test_shouldThrowBadRequestWhenPrioridadeIsZero() {
        CreateTarefaRequestDTO dto = new CreateTarefaRequestDTO("Revisar PR", "Fazer revisão do PR",0);

        ResponseStatusException exception = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.create(dto, "resp@insper.edu.br"));

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    public void test_shouldReturnAllTarefasWhenListIsCalled() {
        Mockito.when(tarefaRepository.findAll())
                .thenReturn(List.of(new Tarefa(), new Tarefa()));

        List<Tarefa> response = tarefaService.list();

        Assertions.assertEquals(2, response.size());
    }

    @Test
    public void test_shouldDeleteTarefaWhenExists() {
        Mockito.when(tarefaRepository.existsById(1L))
                .thenReturn(true);

        tarefaService.delete(1L);

        Mockito.verify(tarefaRepository).deleteById(1L);
    }

    @Test
    public void test_shouldThrowNotFoundWhenDeleteTarefaDoesNotExist() {
        Mockito.when(tarefaRepository.existsById(1L))
                .thenReturn(false);

        ResponseStatusException exception = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.delete(1L));

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
