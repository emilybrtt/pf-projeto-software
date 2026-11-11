package br.insper.cursos.courses;

import br.insper.cursos.courses.dto.CreateCourseRequestDTO;
// import br.insper.cursos.courses.queue.CourseEventProducer;
// import br.insper.cursos.courses.queue.CourseEventType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository repository;
    // Fila/Redis desativada (dava erro de conexão na EC2). CourseEventProducer
    // não é mais um bean do Spring (ver @Service comentado na própria classe),
    // então ele não pode mais ser injetado aqui. Ver QUEUE_EC2_SETUP.md.
    // private final CourseEventProducer eventProducer;

    public CourseService(CourseRepository repository) {
        this.repository = repository;
    }

    public Course create(CreateCourseRequestDTO request, String adminEmail) {
        Course course = new Course(
                request.code(),
                request.name(),
                request.instructor(),
                adminEmail
        );
        course = repository.save(course);
        // Avisava a fila que um curso novo foi criado (ver CourseEventProducer).
        // Comentado junto com a fila/Redis (erro de conexão na EC2).
        // eventProducer.publish(course.getId(), course.getCode(), CourseEventType.CREATED);
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
        // Mesma ideia do create(): avisava a fila que o curso foi excluído.
        // Comentado junto com a fila/Redis (erro de conexão na EC2).
        // eventProducer.publish(course.getId(), course.getCode(), CourseEventType.DELETED);
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