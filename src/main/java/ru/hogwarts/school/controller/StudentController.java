package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.StudentDTO;
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
    private final StudentService studentService;
    private final StudentMapper studentMapper;
    private final FacultyMapper facultyMapper;

    public StudentController(StudentService studentService,
                             StudentMapper studentMapper,
                             FacultyMapper facultyMapper) {
        this.studentService = studentService;
        this.studentMapper = studentMapper;
        this.facultyMapper = facultyMapper;
    }

    @PostMapping
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentDTO studentDTO) {
        Student student = studentMapper.toEntity(studentDTO);
        Student createdStudent = studentService.createStudent(student);
        StudentDTO createdDTO = studentMapper.toDTO(createdStudent);
        return ResponseEntity.ok(createdDTO);
    }

    @GetMapping("{id}")
    public ResponseEntity<StudentDTO> getStudent(@PathVariable long id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        StudentDTO studentDTO = studentMapper.toDTO(student);
        return ResponseEntity.ok(studentDTO);
    }

    @GetMapping("faculty/{id}")
    public ResponseEntity<SimpleFacultyDTO> getStudentFaculty(@PathVariable long id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        Faculty faculty = student.getFaculty();
        if (faculty == null) {
            return ResponseEntity.noContent().build();
        }
        SimpleFacultyDTO facultyDTO = facultyMapper.toSimpleDTO(faculty);
        return ResponseEntity.ok(facultyDTO);
    }

    @GetMapping("age/{age}")
    public Collection<StudentDTO> getStudentsByAge(@PathVariable int age) {
        return studentService.getStudentsByAge(age).stream()
                .map(studentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("agebetween/{min}-{max}")
    public Collection<StudentDTO> getStudentsByAgeBetween(@PathVariable int min, @PathVariable int max) {
        return studentService.getStudentsByAgeBetween(min, max).stream()
                .map(studentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @PutMapping()
    public ResponseEntity<StudentDTO> updateStudent(@RequestBody StudentDTO studentDTO) {
        Student student = studentMapper.toEntity(studentDTO);
        Student updatedStudent = studentService.updateStudent(student.getId(), student);
        if (updatedStudent == null) {
            return ResponseEntity.notFound().build();
        }
        StudentDTO updatedDTO = studentMapper.toDTO(updatedStudent);
        return ResponseEntity.ok(updatedDTO);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<StudentDTO> deleteStudent(@PathVariable long id) {
        Student student = studentService.getStudentById(id);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        Student deletedStudent = studentService.deleteStudent(id);
        StudentDTO deletedDTO = studentMapper.toDTO(deletedStudent);
        return ResponseEntity.ok(deletedDTO);
    }
}