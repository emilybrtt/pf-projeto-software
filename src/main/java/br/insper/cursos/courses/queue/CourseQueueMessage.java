package br.insper.cursos.courses.queue;

import java.time.LocalDateTime;

// Formato da mensagem que vai dentro da fila (serializada como JSON pelo producer,
// desserializada de volta pelo consumer). Equivalente ao PartidaQueueMessage do
// exemplo do professor, só que usando record em vez de POJO+Lombok.
public record CourseQueueMessage(
        Long courseId,
        String code,
        CourseEventType eventType,
        LocalDateTime timestamp
) {
}
