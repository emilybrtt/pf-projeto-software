package br.insper.cursos.courses;

public enum CourseStatus {
    DISPONIVEL,
    CANCELADO,
    EXCLUIDO,
    // usado apenas pela versão de delete com aprovação do admin (ver CourseService/CourseController)
    EXCLUSAO_PENDENTE
}