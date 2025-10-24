package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import java.util.List;
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
    @ResponseStatus(HttpStatus.CREATED)
    public StudentDTO createStudent(@RequestBody StudentDTO studentDTO) {
        logger.info("Received request to create student: {}", studentDTO.getName());

        Student student = studentMapper.toEntity(studentDTO);
        Student createdStudent = studentService.createStudent(student);
        StudentDTO createdDTO = studentMapper.toDTO(createdStudent);
        logger.info("Successfully created student with id: {}", createdStudent.getId());
        return createdDTO;
    }

    @GetMapping("{id}")
    public StudentDTO getStudent(@PathVariable long id) {
        logger.info("Received request to get student by id: {}", id);

        Student student = studentService.getStudentById(id);
        StudentDTO studentDTO = studentMapper.toDTO(student);
        logger.info("Successfully retrieved student with id: {}", id);
        return studentDTO;
    }

    @GetMapping("count")
    public Integer getCountOfStudents() {
        logger.info("Received request to get student count");

        Integer count = studentService.getCountOfStudents();
        logger.info("Student count: {}", count);
        return count;
    }

    @GetMapping("faculty/{id}")
    public SimpleFacultyDTO getStudentFaculty(@PathVariable long id) {
        logger.info("Received request to get faculty for student id: {}", id);

        Student student = studentService.getStudentById(id);
        Faculty faculty = student.getFaculty();

        if (faculty == null) {
            logger.info("Student with id: {} has no faculty assigned", id);
            return null;
        }

        SimpleFacultyDTO facultyDTO = facultyMapper.toSimpleDTO(faculty);
        logger.info("Successfully retrieved faculty for student id: {}", id);
        return facultyDTO;
    }

    @GetMapping("age/avg")
    public Float getAvgAgeOfStudents() {
        logger.info("Received request to get average age of students");

        Float avgAge = studentService.getAvgAgeOfStudents();
        logger.info("Average student age: {}", avgAge);
        return avgAge;
    }

    @GetMapping("age/{age}")
    public Collection<StudentDTO> getStudentsByAge(@PathVariable int age) {
        logger.info("Received request to get students by age: {}", age);

        Collection<StudentDTO> students = studentService.getStudentsByAge(age)
                .stream()
                .map(studentMapper::toDTO)
                .collect(Collectors.toList());
        logger.info("Found {} students with age: {}", students.size(), age);
        return students;
    }

    @GetMapping("agebetween/{min}-{max}")
    public Collection<StudentDTO> getStudentsByAgeBetween(@PathVariable int min, @PathVariable int max) {
        logger.info("Received request to get students by age between: {} and {}", min, max);

        Collection<StudentDTO> students = studentService.getStudentsByAgeBetween(min, max)
                .stream()
                .map(studentMapper::toDTO)
                .collect(Collectors.toList());
        logger.info("Found {} students with age between {} and {}", students.size(), min, max);
        return students;
    }

    @GetMapping("last")
    public Collection<Student> getLastFiveStudents() {
        logger.info("Received request to get last five students");

        Collection<Student> students = studentService.getLastFiveStudents();
        logger.info("Retrieved {} last students", students.size());
        return students;
    }

    @PutMapping
    public StudentDTO updateStudent(@RequestBody StudentDTO studentDTO) {
        logger.info("Received request to update student with id: {}", studentDTO.getId());

        Student student = studentMapper.toEntity(studentDTO);
        Student updatedStudent = studentService.updateStudent(student.getId(), student);
        StudentDTO updatedDTO = studentMapper.toDTO(updatedStudent);
        logger.info("Successfully updated student with id: {}", studentDTO.getId());
        return updatedDTO;
    }

    @DeleteMapping("{id}")
    public StudentDTO deleteStudent(@PathVariable long id) {
        logger.info("Received request to delete student with id: {}", id);

        Student student = studentService.getStudentById(id);
        Student deletedStudent = studentService.deleteStudent(id);
        StudentDTO deletedDTO = studentMapper.toDTO(deletedStudent);
        logger.info("Successfully deleted student with id: {}", id);
        return deletedDTO;
    }


    @GetMapping("name/starts-with-a")
    public List<String> getStudentsNamesStartsWithA() {
        logger.info("Received request to fetching student names starting with 'A'");

        List<String> result = studentService.getStudentNamesStartingWithA();
        logger.info("Found {} names starting with 'A'", result.size());
        return result;
    }

    @GetMapping("age/average-all")
    public Double getAverageAgeOfAllStudents() {
        logger.info("Received request to calculating average age of all students");

        Double averageAge = studentService.getAverageAgeOfAllStudents();
        logger.info("Average age: {}", averageAge);
        return averageAge;
    }

    @GetMapping("calculate-sum")
    public Long calculateSum() {
        logger.info("Received request to calculating optimized sum");

        Long sum = studentService.calculateSum();
        logger.info("Calculation completed. Result: {}", sum);
        return sum;
    }

    // Обработчики исключений
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(StudentNotFoundException.class)
    public String handleStudentNotFound(StudentNotFoundException e) {
        logger.warn("Student not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Validation error: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({StudentProcessingException.class, Exception.class})
    public String handleServerErrors(Exception e) {
        logger.error("Server error: {}", e.getMessage());
        return "Internal server error";
    }
}