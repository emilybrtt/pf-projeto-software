package br.insper.cursos.courses.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class CreateCourseRequestDTOTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void tearDown() {
        validatorFactory.close();
    }

    @Test
    public void test_shouldHaveNoViolationsWhenAllFieldsArePresent() {
        CreateCourseRequestDTO dto = new CreateCourseRequestDTO("CC101", "Introdução à Computação", "Prof. Fulano");

        Set<ConstraintViolation<CreateCourseRequestDTO>> violations = validator.validate(dto);

        Assertions.assertTrue(violations.isEmpty());
    }

    @Test
    public void test_shouldHaveViolationsWhenCodeIsBlank() {
        CreateCourseRequestDTO dto = new CreateCourseRequestDTO("", "Introdução à Computação", "Prof. Fulano");

        Set<ConstraintViolation<CreateCourseRequestDTO>> violations = validator.validate(dto);

        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void test_shouldHaveViolationsWhenNameIsNull() {
        CreateCourseRequestDTO dto = new CreateCourseRequestDTO("CC101", null, "Prof. Fulano");

        Set<ConstraintViolation<CreateCourseRequestDTO>> violations = validator.validate(dto);

        Assertions.assertFalse(violations.isEmpty());
    }

    @Test
    public void test_shouldHaveViolationsWhenInstructorIsBlank() {
        CreateCourseRequestDTO dto = new CreateCourseRequestDTO("CC101", "Introdução à Computação", "   ");

        Set<ConstraintViolation<CreateCourseRequestDTO>> violations = validator.validate(dto);

        Assertions.assertFalse(violations.isEmpty());
    }
}
