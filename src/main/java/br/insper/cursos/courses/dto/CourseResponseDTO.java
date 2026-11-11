package br.insper.cursos.courses.dto;

import br.insper.cursos.courses.Course;
import br.insper.cursos.courses.CourseStatus;

import java.time.LocalDateTime;

public record CourseResponseDTO(
        Long id,
        String code,
        String name,
        String instructor,
        LocalDateTime createdAt,
        CourseStatus status,
        String adminEmail
) {
    public static CourseResponseDTO from(Course course) {
        return new CourseResponseDTO(
                course.getId(),
                course.getCode(),
                course.getName(),
                course.getInstructor(),
                course.getCreatedAt(),
                course.getStatus(),
                course.getAdminEmail()
        );
    }
}
