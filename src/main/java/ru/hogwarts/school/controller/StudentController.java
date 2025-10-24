package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.StudentDTO;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.exception.StudentProcessingException;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.stream.Collectors;

@RequestMapping("student")
@RestController
public class StudentController {
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    private final StudentService studentService;
    private final StudentMapper studentMapper;
    private final FacultyMapper facultyMapper;

    public StudentController(StudentService studentService, StudentMapper studentMapper, FacultyMapper facultyMapper) {
        this.studentService = studentService;
        this.studentMapper = studentMapper;
        this.facultyMapper = facultyMapper;
    }

    @PostMapping
    public ResponseEntity<?> createStudent(@RequestBody StudentDTO studentDTO) {
        logger.info("Received request to create student: {}", studentDTO.getName());

        try {
            Student student = studentMapper.toEntity(studentDTO);
            Student createdStudent = studentService.createStudent(student);
            StudentDTO createdDTO = studentMapper.toDTO(createdStudent);
            logger.info("Successfully created student with id: {}", createdStudent.getId());
            return ResponseEntity.ok(createdDTO);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error during student creation: {}", studentDTO.getName(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StudentProcessingException e) {
            logger.error("Error creating student: {}", studentDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating student: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating student: {}", studentDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error creating student");
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getStudent(@PathVariable long id) {
        logger.info("Received request to get student by id: {}", id);

        try {
            Student student = studentService.getStudentById(id);
            StudentDTO studentDTO = studentMapper.toDTO(student);
            logger.info("Successfully retrieved student with id: {}", id);
            return ResponseEntity.ok(studentDTO);

        } catch (StudentNotFoundException e) {
            logger.warn("Student not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving student with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving student");
        }
    }

    @GetMapping("count")
    public ResponseEntity<?> getCountOfStudents() {
        logger.info("Received request to get student count");

        try {
            Integer count = studentService.getCountOfStudents();
            logger.info("Student count: {}", count);
            return ResponseEntity.ok(count);

        } catch (StudentProcessingException e) {
            logger.error("Error retrieving student count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving student count");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving student count", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving student count");
        }
    }

    @GetMapping("faculty/{id}")
    public ResponseEntity<?> getStudentFaculty(@PathVariable long id) {
        logger.info("Received request to get faculty for student id: {}", id);

        try {
            Student student = studentService.getStudentById(id);
            Faculty faculty = student.getFaculty();

            if (faculty == null) {
                logger.info("Student with id: {} has no faculty assigned", id);
                return ResponseEntity.noContent().build();
            }

            SimpleFacultyDTO facultyDTO = facultyMapper.toSimpleDTO(faculty);
            logger.info("Successfully retrieved faculty for student id: {}", id);
            return ResponseEntity.ok(facultyDTO);

        } catch (StudentNotFoundException e) {
            logger.warn("Student not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving faculty for student id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving student faculty");
        }
    }

    @GetMapping("age/avg")
    public ResponseEntity<?> getAvgAgeOfStudents() {
        logger.info("Received request to get average age of students");

        try {
            Float avgAge = studentService.getAvgAgeOfStudents();
            logger.info("Average student age: {}", avgAge);
            return ResponseEntity.ok(avgAge);

        } catch (StudentProcessingException e) {
            logger.error("Error calculating average age of students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error calculating average age");
        } catch (Exception e) {
            logger.error("Unexpected error calculating average age of students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error calculating average age");
        }
    }

    @GetMapping("age/{age}")
    public ResponseEntity<?> getStudentsByAge(@PathVariable int age) {
        logger.info("Received request to get students by age: {}", age);

        try {
            Collection<StudentDTO> students = studentService.getStudentsByAge(age)
                    .stream()
                    .map(studentMapper::toDTO)
                    .collect(Collectors.toList());
            logger.info("Found {} students with age: {}", students.size(), age);
            return ResponseEntity.ok(students);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid age parameter: {}", age, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StudentProcessingException e) {
            logger.error("Error retrieving students by age: {}", age, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving students by age");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving students by age: {}", age, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving students");
        }
    }

    @GetMapping("agebetween/{min}-{max}")
    public ResponseEntity<?> getStudentsByAgeBetween(@PathVariable int min, @PathVariable int max) {
        logger.info("Received request to get students by age between: {} and {}", min, max);

        try {
            Collection<StudentDTO> students = studentService.getStudentsByAgeBetween(min, max)
                    .stream()
                    .map(studentMapper::toDTO)
                    .collect(Collectors.toList());
            logger.info("Found {} students with age between {} and {}", students.size(), min, max);
            return ResponseEntity.ok(students);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid age range parameters: min={}, max={}", min, max, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StudentProcessingException e) {
            logger.error("Error retrieving students by age range: {} - {}", min, max, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving students by age range");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving students by age range: {} - {}", min, max, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving students");
        }
    }

    @GetMapping("last")
    public ResponseEntity<?> getLastFiveStudents() {
        logger.info("Received request to get last five students");

        try {
            Collection<Student> students = studentService.getLastFiveStudents();
            logger.info("Retrieved {} last students", students.size());
            return ResponseEntity.ok(students);

        } catch (StudentProcessingException e) {
            logger.error("Error retrieving last five students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving last five students");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving last five students", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving last students");
        }
    }

    @PutMapping()
    public ResponseEntity<?> updateStudent(@RequestBody StudentDTO studentDTO) {
        logger.info("Received request to update student with id: {}", studentDTO.getId());

        try {
            Student student = studentMapper.toEntity(studentDTO);
            Student updatedStudent = studentService.updateStudent(student.getId(), student);
            StudentDTO updatedDTO = studentMapper.toDTO(updatedStudent);
            logger.info("Successfully updated student with id: {}", studentDTO.getId());
            return ResponseEntity.ok(updatedDTO);

        } catch (StudentNotFoundException e) {
            logger.warn("Student not found for update with id: {}", studentDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during student update with id: {}", studentDTO.getId(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StudentProcessingException e) {
            logger.error("Error updating student with id: {}", studentDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating student: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating student with id: {}", studentDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error updating student");
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable long id) {
        logger.info("Received request to delete student with id: {}", id);

        try {
            Student student = studentService.getStudentById(id);
            Student deletedStudent = studentService.deleteStudent(id);
            StudentDTO deletedDTO = studentMapper.toDTO(deletedStudent);
            logger.info("Successfully deleted student with id: {}", id);
            return ResponseEntity.ok(deletedDTO);

        } catch (StudentNotFoundException e) {
            logger.warn("Student not found for deletion with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id for deletion: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StudentProcessingException e) {
            logger.error("Error deleting student with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting student: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting student with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error deleting student");
        }
    }
}