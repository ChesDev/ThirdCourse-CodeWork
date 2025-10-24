package ru.hogwarts.school.exception;

public class FacultyProcessingException extends RuntimeException {
    public FacultyProcessingException(String message) {
        super(message);
    }

    public FacultyProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}