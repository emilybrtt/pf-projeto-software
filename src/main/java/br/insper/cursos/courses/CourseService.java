package br.insper.cursos.courses;

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

    public Course create(CreateCourseRequest request, String adminEmail) {
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

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Curso não encontrado");
        }
        repository.deleteById(id);
    }
}