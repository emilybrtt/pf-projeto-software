package br.insper.cursos.courses;

import br.insper.cursos.courses.dto.CourseResponseDTO;
import br.insper.cursos.courses.dto.CreateCourseRequestDTO;
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
    public CourseResponseDTO create(@AuthenticationPrincipal Jwt jwt,
                         @RequestBody CreateCourseRequestDTO request) {
        checkAdmin(jwt);
        String email = jwt.getClaimAsString(NAMESPACE + "email");
        Course course = service.create(request, email);
        return CourseResponseDTO.from(course);
    }

    @GetMapping
    public List<CourseResponseDTO> list() {
        return service.list().stream()
                .map(CourseResponseDTO::from)
                .toList();
    }

    @GetMapping("/{id}")
    public CourseResponseDTO getById(@PathVariable Long id) {
        return CourseResponseDTO.from(service.getById(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public CourseResponseDTO requestDelete(@PathVariable Long id) {
        return CourseResponseDTO.from(service.requestDelete(id));
    }

    @PostMapping("/{id}/approve-delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveDelete(@AuthenticationPrincipal Jwt jwt,
                               @PathVariable Long id) {
        checkAdmin(jwt);
        service.approveDelete(id);
    }

    private void checkAdmin(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(NAMESPACE + "roles");
        if (roles == null || !roles.contains("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Apenas ADMIN pode executar esta ação");
        }
    }
}