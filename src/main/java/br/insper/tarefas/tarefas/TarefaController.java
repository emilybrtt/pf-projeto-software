package br.insper.tarefas.tarefas;

import br.insper.tarefas.tarefas.dto.CreateTarefaRequestDTO;
import br.insper.tarefas.tarefas.dto.TarefaResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tarefas")
@CrossOrigin(origins = "*")
public class TarefaController {

    private static final String NAMESPACE = "https://social-insper.com/";

    private final TarefaService service;

    public TarefaController(TarefaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaResponseDTO create(@AuthenticationPrincipal Jwt jwt,
                                    @RequestBody CreateTarefaRequestDTO request) {

        String email = jwt.getClaimAsString(NAMESPACE + "email");
        Tarefa tarefa = service.create(request, email);
        return TarefaResponseDTO.from(tarefa);
    }

    @GetMapping
    public List<TarefaResponseDTO> list() {
        return service.list().stream()
                .map(TarefaResponseDTO::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
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
