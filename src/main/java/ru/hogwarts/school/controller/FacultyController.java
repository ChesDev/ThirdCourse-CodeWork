package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.SimpleStudentDTO;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.FacultyProcessingException;
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
    private static final Logger logger = LoggerFactory.getLogger(FacultyController.class);

    private final FacultyService facultyService;
    private final StudentService studentService;
    private final FacultyMapper facultyMapper;

    public FacultyController(FacultyService facultyService, StudentService studentService, FacultyMapper facultyMapper) {
        this.facultyService = facultyService;
        this.studentService = studentService;
        this.facultyMapper = facultyMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FacultyDTO createFaculty(@RequestBody FacultyDTO facultyDTO) {
        logger.info("Received request to create faculty: {}", facultyDTO.getName());

        Faculty faculty = facultyMapper.toEntity(facultyDTO);
        Faculty createdFaculty = facultyService.createFaculty(faculty);
        FacultyDTO createdDTO = facultyMapper.toDTO(createdFaculty);
        logger.info("Successfully created faculty with id: {}", createdFaculty.getId());
        return createdDTO;
    }

    @GetMapping("{id}")
    public FacultyDTO getFacultyById(@PathVariable long id) {
        logger.info("Received request to get faculty by id: {}", id);

        Faculty faculty = facultyService.getFacultyById(id);
        FacultyDTO facultyDTO = facultyMapper.toDTO(faculty);
        logger.info("Successfully retrieved faculty with id: {}", id);
        return facultyDTO;
    }

    @GetMapping("students/{id}")
    public Collection<SimpleStudentDTO> getFacultyStudents(@PathVariable int id) {
        logger.info("Received request to get students for faculty id: {}", id);

        Faculty faculty = facultyService.getFacultyById(id);
        Collection<Student> students = studentService.getStudentsByFacultyId(id);
        Collection<SimpleStudentDTO> studentDTOs = students.stream()
                .map(student -> new SimpleStudentDTO(student.getId(), student.getName(), student.getAge()))
                .collect(Collectors.toList());
        logger.info("Successfully retrieved {} students for faculty id: {}", studentDTOs.size(), id);
        return studentDTOs;
    }

    @GetMapping
    public Collection<FacultyDTO> getFacultiesByColorOrName(@RequestParam(required = false) String name,
                                                            @RequestParam(required = false) String color) {
        logger.info("Received request to get faculties by name: {} or color: {}", name, color);

        Collection<Faculty> faculties;

        if (name != null && !name.isBlank()) {
            faculties = facultyService.getFacultiesByName(name);
            logger.debug("Found {} faculties by name: {}", faculties.size(), name);
        } else if (color != null && !color.isBlank()) {
            faculties = facultyService.getFacultiesByColor(color);
            logger.debug("Found {} faculties by color: {}", faculties.size(), color);
        } else {
            faculties = facultyService.getAllFaculties();
            logger.debug("Found all {} faculties", faculties.size());
        }

        return faculties.stream()
                .map(facultyMapper::toDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("longest-name")
    public String getLongestFacultyName() {
        logger.info("Received request to finding the longest faculty name");

        String longestName = facultyService.getLongestFacultyName();
        logger.info("GET /faculty/longest-name - Longest name: {} ({} characters)", longestName, longestName.length());
        return longestName;
    }

    @PutMapping
    public FacultyDTO updateFaculty(@RequestBody FacultyDTO facultyDTO) {
        logger.info("Received request to update faculty with id: {}", facultyDTO.getId());

        Faculty faculty = facultyMapper.toEntity(facultyDTO);
        Faculty updatedFaculty = facultyService.updateFaculty(faculty.getId(), faculty);
        FacultyDTO updatedDTO = facultyMapper.toDTO(updatedFaculty);
        logger.info("Successfully updated faculty with id: {}", facultyDTO.getId());
        return updatedDTO;
    }

    @DeleteMapping("{id}")
    public SimpleFacultyDTO deleteFaculty(@PathVariable long id) {
        logger.info("Received request to delete faculty with id: {}", id);

        Faculty faculty = facultyService.getFacultyById(id);
        SimpleFacultyDTO facultyDTO = facultyMapper.toSimpleDTO(faculty);
        Faculty deletedFaculty = facultyService.deleteFaculty(id);
        logger.info("Successfully deleted faculty with id: {}", id);
        return facultyDTO;
    }

    // Обработчики исключений
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FacultyNotFoundException.class)
    public String handleFacultyNotFound(FacultyNotFoundException e) {
        logger.warn("Faculty not found: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Validation error: {}", e.getMessage());
        return e.getMessage();
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({FacultyProcessingException.class, Exception.class})
    public String handleServerErrors(Exception e) {
        logger.error("Server error: {}", e.getMessage());
        return "Internal server error";
    }
}