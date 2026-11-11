package br.insper.cursos.courses.queue;

// Tipo de evento publicado na fila de cursos (ver CourseEventProducer).
// Dá pra adicionar outros tipos aqui (ex: UPDATED) se precisar publicar mais eventos.
public enum CourseEventType {
    CREATED,
    DELETED
}
