package ru.hogwarts.school.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.SimpleStudentDTO;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.util.Collection;
import java.util.stream.Collectors;

@RequestMapping("faculty")
@RestController
public class FacultyController {
    private final FacultyService facultyService;
    private final StudentService studentService;
    private final FacultyMapper facultyMapper;

    public FacultyController(FacultyService facultyService, StudentService studentService, FacultyMapper facultyMapper) {
        this.facultyService = facultyService;
        this.studentService = studentService;
        this.facultyMapper = facultyMapper;
    }

    @PostMapping
    public ResponseEntity<FacultyDTO> createFaculty(@RequestBody FacultyDTO facultyDTO) {
        Faculty faculty = facultyMapper.toEntity(facultyDTO);
        Faculty createdFaculty = facultyService.createFaculty(faculty);
        FacultyDTO createdDTO = facultyMapper.toDTO(createdFaculty);
        return ResponseEntity.ok(createdDTO);
    }

    @GetMapping("{id}")
    public ResponseEntity<FacultyDTO> getFacultyById(@PathVariable long id) {
        Faculty faculty = facultyService.getFacultyById(id);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }
        FacultyDTO facultyDTO = facultyMapper.toDTO(faculty);
        return ResponseEntity.ok(facultyDTO);
    }

    @GetMapping("students/{id}")
    public ResponseEntity<Collection<SimpleStudentDTO>> getFacultyStudents(@PathVariable int id) {
        Faculty faculty = facultyService.getFacultyById(id);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }
        Collection<Student> students = studentService.getStudentsByFacultyId(id);
        Collection<SimpleStudentDTO> studentDTOs = students.stream().map(student -> new SimpleStudentDTO(student.getId(), student.getName(), student.getAge())).collect(Collectors.toList());
        return ResponseEntity.ok(studentDTOs);
    }

    @GetMapping
    public ResponseEntity<Collection<FacultyDTO>> getFacultiesByColorOrName(@RequestParam(required = false) String name, @RequestParam(required = false) String color) {

        Collection<Faculty> faculties;

        if (name != null && !name.isBlank()) {
            faculties = facultyService.getFacultiesByName(name);
        } else if (color != null && !color.isBlank()) {
            faculties = facultyService.getFacultiesByColor(color);
        } else {
            faculties = facultyService.getAllFaculties();
        }

        Collection<FacultyDTO> facultyDTOs = faculties.stream().map(facultyMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(facultyDTOs);
    }

    @PutMapping
    public ResponseEntity<FacultyDTO> updateFaculty(@RequestBody FacultyDTO facultyDTO) {
        Faculty faculty = facultyMapper.toEntity(facultyDTO);
        Faculty updatedFaculty = facultyService.updateFaculty(faculty.getId(), faculty);
        if (updatedFaculty == null) {
            return ResponseEntity.notFound().build();
        }
        FacultyDTO updatedDTO = facultyMapper.toDTO(updatedFaculty);
        return ResponseEntity.ok(updatedDTO);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<SimpleFacultyDTO> deleteFaculty(@PathVariable long id) {
        Faculty faculty = facultyService.getFacultyById(id);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }

        SimpleFacultyDTO facultyDTO = facultyMapper.toSimpleDTO(faculty);
        Faculty deletedFaculty = facultyService.deleteFaculty(id);
        return ResponseEntity.ok(facultyDTO);
    }
}