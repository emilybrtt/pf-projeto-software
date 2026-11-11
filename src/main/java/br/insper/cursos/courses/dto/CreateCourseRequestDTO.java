package br.insper.cursos.courses.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCourseRequestDTO(
        @NotBlank(message = "code é obrigatório")
        String code,

        @NotBlank(message = "name é obrigatório")
        String name,

        @NotBlank(message = "instructor é obrigatório")
        String instructor
) {}
