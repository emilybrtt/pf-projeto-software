package br.insper.cursos.courses;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface Courserepository extends MongoRepository<Course, String> {
}