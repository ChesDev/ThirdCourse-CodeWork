package ru.hogwarts.school.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.SimpleStudentDTO;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.FacultyProcessingException;
import ru.hogwarts.school.exception.StudentProcessingException;
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
    public ResponseEntity<?> createFaculty(@RequestBody FacultyDTO facultyDTO) {
        logger.info("Received request to create faculty: {}", facultyDTO.getName());

        try {
            Faculty faculty = facultyMapper.toEntity(facultyDTO);
            Faculty createdFaculty = facultyService.createFaculty(faculty);
            FacultyDTO createdDTO = facultyMapper.toDTO(createdFaculty);
            logger.info("Successfully created faculty with id: {}", createdFaculty.getId());
            return ResponseEntity.ok(createdDTO);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error during faculty creation: {}", facultyDTO.getName(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FacultyProcessingException e) {
            logger.error("Error creating faculty: {}", facultyDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating faculty: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating faculty: {}", facultyDTO.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error creating faculty");
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getFacultyById(@PathVariable long id) {
        logger.info("Received request to get faculty by id: {}", id);

        try {
            Faculty faculty = facultyService.getFacultyById(id);
            FacultyDTO facultyDTO = facultyMapper.toDTO(faculty);
            logger.info("Successfully retrieved faculty with id: {}", id);
            return ResponseEntity.ok(facultyDTO);

        } catch (FacultyNotFoundException e) {
            logger.warn("Faculty not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid faculty id: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving faculty with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving faculty");
        }
    }

    @GetMapping("students/{id}")
    public ResponseEntity<?> getFacultyStudents(@PathVariable int id) {
        logger.info("Received request to get students for faculty id: {}", id);

        try {
            Faculty faculty = facultyService.getFacultyById(id);
            Collection<Student> students = studentService.getStudentsByFacultyId(id);
            Collection<SimpleStudentDTO> studentDTOs = students.stream()
                    .map(student -> new SimpleStudentDTO(student.getId(), student.getName(), student.getAge()))
                    .collect(Collectors.toList());
            logger.info("Successfully retrieved {} students for faculty id: {}", studentDTOs.size(), id);
            return ResponseEntity.ok(studentDTOs);

        } catch (FacultyNotFoundException e) {
            logger.warn("Faculty not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid faculty id: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StudentProcessingException e) {
            logger.error("Error retrieving students for faculty id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving students");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving students for faculty id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving students");
        }
    }

    @GetMapping
    public ResponseEntity<?> getFacultiesByColorOrName(@RequestParam(required = false) String name,
                                                       @RequestParam(required = false) String color) {
        logger.info("Received request to get faculties by name: {} or color: {}", name, color);

        try {
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

            Collection<FacultyDTO> facultyDTOs = faculties.stream()
                    .map(facultyMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(facultyDTOs);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid search parameters - name: {}, color: {}", name, color, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FacultyProcessingException e) {
            logger.error("Error retrieving faculties by name: {} or color: {}", name, color, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving faculties");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving faculties", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving faculties");
        }
    }

    @GetMapping("longest-name")
    public ResponseEntity<String> getLongestFacultyName() {
        String longestName = facultyService.getLongestFacultyName();
        return ResponseEntity.ok(longestName);
    }

    @PutMapping
    public ResponseEntity<?> updateFaculty(@RequestBody FacultyDTO facultyDTO) {
        logger.info("Received request to update faculty with id: {}", facultyDTO.getId());

        try {
            Faculty faculty = facultyMapper.toEntity(facultyDTO);
            Faculty updatedFaculty = facultyService.updateFaculty(faculty.getId(), faculty);
            FacultyDTO updatedDTO = facultyMapper.toDTO(updatedFaculty);
            logger.info("Successfully updated faculty with id: {}", facultyDTO.getId());
            return ResponseEntity.ok(updatedDTO);

        } catch (FacultyNotFoundException e) {
            logger.warn("Faculty not found for update with id: {}", facultyDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during faculty update with id: {}", facultyDTO.getId(), e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FacultyProcessingException e) {
            logger.error("Error updating faculty with id: {}", facultyDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating faculty: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating faculty with id: {}", facultyDTO.getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error updating faculty");
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteFaculty(@PathVariable long id) {
        logger.info("Received request to delete faculty with id: {}", id);

        try {
            Faculty faculty = facultyService.getFacultyById(id);
            SimpleFacultyDTO facultyDTO = facultyMapper.toSimpleDTO(faculty);
            Faculty deletedFaculty = facultyService.deleteFaculty(id);
            logger.info("Successfully deleted faculty with id: {}", id);
            return ResponseEntity.ok(facultyDTO);

        } catch (FacultyNotFoundException e) {
            logger.warn("Faculty not found for deletion with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid faculty id for deletion: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (FacultyProcessingException e) {
            logger.error("Error deleting faculty with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting faculty: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting faculty with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error deleting faculty");
        }
    }
}