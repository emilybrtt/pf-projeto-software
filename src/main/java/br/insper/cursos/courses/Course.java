package br.insper.cursos.courses;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "courses")
public class Course {

    @Id
    private String id;

    private String code;
    private String name;
    private String instructor;
    private LocalDateTime createdAt;
    private CourseStatus status;
    private String adminEmail;

    public Course() {}

    public Course(String code, String name, String instructor, String adminEmail) {
        this.code = code;
        this.name = name;
        this.instructor = instructor;
        this.adminEmail = adminEmail;
        this.createdAt = LocalDateTime.now();
        this.status = CourseStatus.DISPONIVEL;
    }

    public String getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getInstructor() { return instructor; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public CourseStatus getStatus() { return status; }
    public String getAdminEmail() { return adminEmail; }

    public void setStatus(CourseStatus status) { this.status = status; }
}