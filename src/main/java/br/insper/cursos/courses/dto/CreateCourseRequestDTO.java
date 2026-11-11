package br.insper.cursos.courses.dto;

public record CreateCourseRequestDTO(
        String code,
        String name,
        String instructor
) {}
