package br.insper.cursos.courses;

import br.insper.cursos.courses.dto.CreateCourseRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CourseService {

    private final CourseRepository repository;

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
        return repository.save(course);
    }

    public List<Course> list() {
        return repository.findAll();
    }

    public Course getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado"));
    }

    public Course requestDelete(Long id) {
        Course course = getById(id);
        course.setStatus(CourseStatus.EXCLUSAO_PENDENTE);
        return repository.save(course);
    }

    public void approveDelete(Long id) {
        Course course = getById(id);
        if (course.getStatus() != CourseStatus.EXCLUSAO_PENDENTE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Curso não possui solicitação de exclusão pendente");
        }
        repository.deleteById(id);
    }
}