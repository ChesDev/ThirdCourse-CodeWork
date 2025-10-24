package ru.hogwarts.school.exception;

public class StudentProcessingException extends RuntimeException {
    public StudentProcessingException(String message) {
        super(message);
    }

    public StudentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}