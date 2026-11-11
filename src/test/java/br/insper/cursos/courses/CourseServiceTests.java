package br.insper.cursos.courses;

// import br.insper.cursos.courses.queue.CourseEventProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTests {

    @InjectMocks
    private CourseService courseService;

    @Mock
    private CourseRepository courseRepository;

    // Fila/Redis desativada (ver CourseService). Mock não é mais necessário
    // porque CourseEventProducer não é mais injetado no CourseService.
    // @Mock
    // private CourseEventProducer eventProducer;

    // ---- Delete ativo: soft delete simples ----

    @Test
    public void test_shouldSetStatusToExcluidoWhenSoftDelete() {
        Course course = new Course("CC101", "Introdução à Computação", "Prof. Fulano", "admin@insper.edu.br");

        Mockito.when(courseRepository.findById(1L))
                .thenReturn(Optional.of(course));
        Mockito.when(courseRepository.save(Mockito.any()))
                .thenReturn(course);

        Course response = courseService.softDelete(1L);

        Assertions.assertEquals(CourseStatus.EXCLUIDO, response.getStatus());
        Mockito.verify(courseRepository).save(course);
    }

    @Test
    public void test_shouldThrowNotFoundWhenSoftDeleteCourseDoesNotExist() {
        Mockito.when(courseRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = Assertions.assertThrows(
                ResponseStatusException.class,
                () -> courseService.softDelete(1L));

        Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    // ---- Versão alternativa: delete em duas etapas com aprovação do admin ----
    // Se descomentar requestDelete/approveDelete no CourseService, descomente os
    // testes abaixo também (e comente os dois de soft delete acima, se quiser
    // evitar sobreposição de cobertura).
    //
    // @Test
    // public void test_shouldSetStatusToExclusaoPendenteWhenRequestDelete() {
    //     Course course = new Course("CC101", "Introdução à Computação", "Prof. Fulano", "admin@insper.edu.br");
    //
    //     Mockito.when(courseRepository.findById(1L))
    //             .thenReturn(Optional.of(course));
    //     Mockito.when(courseRepository.save(Mockito.any()))
    //             .thenReturn(course);
    //
    //     Course response = courseService.requestDelete(1L);
    //
    //     Assertions.assertEquals(CourseStatus.EXCLUSAO_PENDENTE, response.getStatus());
    //     Mockito.verify(courseRepository).save(course);
    // }
    //
    // @Test
    // public void test_shouldThrowNotFoundWhenRequestDeleteCourseDoesNotExist() {
    //     Mockito.when(courseRepository.findById(1L))
    //             .thenReturn(Optional.empty());
    //
    //     ResponseStatusException exception = Assertions.assertThrows(
    //             ResponseStatusException.class,
    //             () -> courseService.requestDelete(1L));
    //
    //     Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    // }
    //
    // @Test
    // public void test_shouldDeleteCourseWhenApproveDeleteAndStatusIsExclusaoPendente() {
    //     Course course = new Course("CC101", "Introdução à Computação", "Prof. Fulano", "admin@insper.edu.br");
    //     course.setStatus(CourseStatus.EXCLUSAO_PENDENTE);
    //
    //     Mockito.when(courseRepository.findById(1L))
    //             .thenReturn(Optional.of(course));
    //
    //     courseService.approveDelete(1L);
    //
    //     Mockito.verify(courseRepository).deleteById(1L);
    // }
    //
    // @Test
    // public void test_shouldThrowConflictWhenApproveDeleteAndStatusIsNotPending() {
    //     Course course = new Course("CC101", "Introdução à Computação", "Prof. Fulano", "admin@insper.edu.br");
    //
    //     Mockito.when(courseRepository.findById(1L))
    //             .thenReturn(Optional.of(course));
    //
    //     ResponseStatusException exception = Assertions.assertThrows(
    //             ResponseStatusException.class,
    //             () -> courseService.approveDelete(1L));
    //
    //     Assertions.assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    //     Mockito.verify(courseRepository, Mockito.never()).deleteById(Mockito.any());
    // }
    //
    // @Test
    // public void test_shouldThrowNotFoundWhenApproveDeleteCourseDoesNotExist() {
    //     Mockito.when(courseRepository.findById(1L))
    //             .thenReturn(Optional.empty());
    //
    //     ResponseStatusException exception = Assertions.assertThrows(
    //             ResponseStatusException.class,
    //             () -> courseService.approveDelete(1L));
    //
    //     Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    // }
}
