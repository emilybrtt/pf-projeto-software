package br.insper.cursos.courses;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@RequestBody CreateCourseRequest request) {
        // TODO (etapa Auth0): pegar o e-mail do JWT e checar se é ADMIN
        return service.create(request, "admin-temporario@teste.com");
    }

    @GetMapping
    public List<Course> list() {
        return service.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        // TODO (etapa Auth0): checar se é ADMIN
        service.delete(id);
    }
}