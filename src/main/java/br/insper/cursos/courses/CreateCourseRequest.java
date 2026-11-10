package br.insper.cursos.courses;

public record CreateCourseRequest(
        String code,
        String name,
        String instructor
) {}