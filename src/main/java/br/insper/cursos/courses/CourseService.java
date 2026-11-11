package br.insper.cursos.courses;

import br.insper.cursos.courses.dto.CreateCourseRequestDTO;
import br.insper.cursos.courses.queue.CourseEventProducer;
import br.insper.cursos.courses.queue.CourseEventType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository repository;
    private final CourseEventProducer eventProducer;

    public CourseService(CourseRepository repository, CourseEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    public Course create(CreateCourseRequestDTO request, String adminEmail) {
        Course course = new Course(
                request.code(),
                request.name(),
                request.instructor(),
                adminEmail
        );
        course = repository.save(course);
        // Avisa a fila que um curso novo foi criado (ver CourseEventProducer).
        // Se não precisar de fila, é só remover esta linha + o parâmetro/campo
        // eventProducer do construtor (e o import).
        eventProducer.publish(course.getId(), course.getCode(), CourseEventType.CREATED);
        return course;
    }

    public List<Course> list() {
        return repository.findAll();
    }

    public Course getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado"));
    }

    // ---- Delete ativo: soft delete simples (uma etapa, só ADMIN) ----
    public Course softDelete(Long id) {
        Course course = getById(id);
        course.setStatus(CourseStatus.EXCLUIDO);
        course = repository.save(course);
        // Mesma ideia do create(): avisa a fila que o curso foi excluído.
        eventProducer.publish(course.getId(), course.getCode(), CourseEventType.DELETED);
        return course;
    }

    // ---- Versão alternativa: delete em duas etapas com aprovação do admin ----
    // Se a prova pedir esse fluxo: comente o softDelete acima e descomente os dois
    // métodos abaixo (e o trecho equivalente no CourseController).
    //
    // public Course requestDelete(Long id) {
    //     Course course = getById(id);
    //     course.setStatus(CourseStatus.EXCLUSAO_PENDENTE);
    //     return repository.save(course);
    // }
    //
    // public void approveDelete(Long id) {
    //     Course course = getById(id);
    //     if (course.getStatus() != CourseStatus.EXCLUSAO_PENDENTE) {
    //         throw new ResponseStatusException(HttpStatus.CONFLICT,
    //                 "Curso não possui solicitação de exclusão pendente");
    //     }
    //     repository.deleteById(id);
    // }
}