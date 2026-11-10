package br.insper.cursos.courses;

public record Createcourserequest(
        String code,
        String name,
        String instructor
) {}