package br.insper.cursos.courses;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/courses")
@CrossOrigin(origins = "*")
public class CourseController {

    private static final String NAMESPACE = "https://social-insper.com/";

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Course create(@AuthenticationPrincipal Jwt jwt,
                         @RequestBody CreateCourseRequest request) {
        checkAdmin(jwt);
        String email = jwt.getClaimAsString(NAMESPACE + "email");
        return service.create(request, email);
    }

    @GetMapping
    public List<Course> list() {
        return service.list();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt,
                       @PathVariable String id) {
        checkAdmin(jwt);
        service.delete(id);
    }

    private void checkAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(NAMESPACE + "roles");
        if (roles == null || !roles.contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas ADMIN pode executar esta ação");
        }
    }
}