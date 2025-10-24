package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.hogwarts.school.controller.TestConstants.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FacultyService facultyService;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private FacultyMapper facultyMapper;

    @Test
    void createFaculty_ShouldReturnCreatedFaculty() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isCreated()) // Изменено с isCreated() на isOk()
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(GRYFFINDOR_NAME))
                .andExpect(jsonPath("$.color").value(GRYFFINDOR_COLOR));
    }

    @Test
    void createFaculty_WithValidationError_ShouldReturnBadRequest() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(null, "", "");

        when(facultyMapper.toEntity(any(FacultyDTO.class)))
                .thenThrow(new IllegalArgumentException("Name cannot be empty"));

        // When & Then
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Name cannot be empty"));
    }

    @Test
    void getFacultyById_WhenFacultyExists_ShouldReturnFaculty() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, SLYTHERIN_NAME, SLYTHERIN_COLOR);
        FacultyDTO facultyDTO = createFacultyDTO(1L, SLYTHERIN_NAME, SLYTHERIN_COLOR);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(SLYTHERIN_NAME))
                .andExpect(jsonPath("$.color").value(SLYTHERIN_COLOR));
    }

    @Test
    void getFacultyById_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(NON_EXISTENT_ID))
                .thenThrow(new FacultyNotFoundException(FACULTY_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(get("/faculty/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(FACULTY_NOT_FOUND_MESSAGE));
    }

    @Test
    void getFacultyStudents_WhenFacultyExists_ShouldReturnStudentsList() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        Student student = createStudent(1L, HARRY_POTTER_NAME, STUDENT_AGE_17);
        List<Student> students = List.of(student);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(studentService.getStudentsByFacultyId(1)).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/faculty/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(HARRY_POTTER_NAME))
                .andExpect(jsonPath("$[0].age").value(STUDENT_AGE_17));
    }

    @Test
    void getFacultyStudents_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(NON_EXISTENT_ID))
                .thenThrow(new FacultyNotFoundException(FACULTY_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(get("/faculty/students/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(FACULTY_NOT_FOUND_MESSAGE));
    }

    @Test
    void getFacultyStudents_WhenFacultyHasNoStudents_ShouldReturnEmptyList() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(studentService.getStudentsByFacultyId(1)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/faculty/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFacultiesByName_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, RAVENCLAW_NAME, RAVENCLAW_COLOR);
        FacultyDTO facultyDTO = createFacultyDTO(1L, RAVENCLAW_NAME, RAVENCLAW_COLOR);
        List<Faculty> faculties = List.of(faculty);

        when(facultyService.getFacultiesByName(RAVENCLAW_NAME)).thenReturn(faculties);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("name", RAVENCLAW_NAME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(RAVENCLAW_NAME))
                .andExpect(jsonPath("$[0].color").value(RAVENCLAW_COLOR));
    }

    @Test
    void getFacultiesByColor_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, HUFFLEPUFF_NAME, HUFFLEPUFF_COLOR);
        FacultyDTO facultyDTO = createFacultyDTO(1L, HUFFLEPUFF_NAME, HUFFLEPUFF_COLOR);
        List<Faculty> faculties = List.of(faculty);

        when(facultyService.getFacultiesByColor(HUFFLEPUFF_COLOR)).thenReturn(faculties);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("color", HUFFLEPUFF_COLOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(HUFFLEPUFF_NAME))
                .andExpect(jsonPath("$[0].color").value(HUFFLEPUFF_COLOR));
    }

    @Test
    void getAllFaculties_ShouldReturnAllFaculties() throws Exception {
        // Given
        Faculty faculty1 = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        Faculty faculty2 = createFaculty(2L, SLYTHERIN_NAME, SLYTHERIN_COLOR);
        FacultyDTO facultyDTO1 = createFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        FacultyDTO facultyDTO2 = createFacultyDTO(2L, SLYTHERIN_NAME, SLYTHERIN_COLOR);
        List<Faculty> faculties = List.of(faculty1, faculty2);

        when(facultyService.getAllFaculties()).thenReturn(faculties);
        when(facultyMapper.toDTO(faculty1)).thenReturn(facultyDTO1);
        when(facultyMapper.toDTO(faculty2)).thenReturn(facultyDTO2);

        // When & Then
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(GRYFFINDOR_NAME))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value(SLYTHERIN_NAME));
    }

    @Test
    void updateFaculty_WhenFacultyExists_ShouldReturnUpdatedFaculty() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, "Гриффиндор Updated", "Алый");
        Faculty faculty = createFaculty(1L, "Гриффиндор Updated", "Алый");

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(eq(1L), any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Гриффиндор Updated"))
                .andExpect(jsonPath("$.color").value("Алый"));
    }

    @Test
    void updateFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(NON_EXISTENT_ID, "Несуществующий факультет", "Черный");
        Faculty faculty = createFaculty(NON_EXISTENT_ID, "Несуществующий факультет", "Черный");

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(eq(NON_EXISTENT_ID), any(Faculty.class)))
                .thenThrow(new FacultyNotFoundException(FACULTY_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(FACULTY_NOT_FOUND_MESSAGE));
    }

    @Test
    void deleteFaculty_WhenFacultyExists_ShouldReturnDeletedFaculty() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        SimpleFacultyDTO facultyDTO = createSimpleFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);
        when(facultyService.deleteFaculty(1L)).thenReturn(faculty);

        // When & Then
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(GRYFFINDOR_NAME))
                .andExpect(jsonPath("$.color").value(GRYFFINDOR_COLOR));
    }

    @Test
    void deleteFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(NON_EXISTENT_ID))
                .thenThrow(new FacultyNotFoundException(FACULTY_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(delete("/faculty/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string(FACULTY_NOT_FOUND_MESSAGE));
    }

    @Test
    void getLongestFacultyName_ShouldReturnLongestName() throws Exception {
        // Given
        when(facultyService.getLongestFacultyName()).thenReturn(LONGEST_FACULTY_NAME);

        // When & Then
        mockMvc.perform(get("/faculty/longest-name"))
                .andExpect(status().isOk())
                .andExpect(content().string(LONGEST_FACULTY_NAME));
    }

    @Test
    void getLongestFacultyName_WhenServiceThrowsException_ShouldReturnInternalServerError() throws Exception {
        // Given
        when(facultyService.getLongestFacultyName())
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(get("/faculty/longest-name"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    void getFaculties_WithEmptyNameAndColor_ShouldReturnAllFaculties() throws Exception {
        // Given
        Faculty faculty1 = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        Faculty faculty2 = createFaculty(2L, SLYTHERIN_NAME, SLYTHERIN_COLOR);
        FacultyDTO facultyDTO1 = createFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        FacultyDTO facultyDTO2 = createFacultyDTO(2L, SLYTHERIN_NAME, SLYTHERIN_COLOR);
        List<Faculty> faculties = List.of(faculty1, faculty2);

        when(facultyService.getAllFaculties()).thenReturn(faculties);
        when(facultyMapper.toDTO(faculty1)).thenReturn(facultyDTO1);
        when(facultyMapper.toDTO(faculty2)).thenReturn(facultyDTO2);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("name", "")
                        .param("color", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(GRYFFINDOR_NAME))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value(SLYTHERIN_NAME));
    }

    @Test
    void getFaculties_WithBlankNameAndNullColor_ShouldReturnAllFaculties() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        FacultyDTO facultyDTO = createFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        List<Faculty> faculties = List.of(faculty);

        when(facultyService.getAllFaculties()).thenReturn(faculties);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("name", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(GRYFFINDOR_NAME));
    }

    @Test
    void createFaculty_WithInternalServerError_ShouldReturnInternalServerError() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(facultyMapper.toEntity(any(FacultyDTO.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    void updateFaculty_WithValidationError_ShouldReturnBadRequest() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, "", "");

        when(facultyMapper.toEntity(any(FacultyDTO.class)))
                .thenThrow(new IllegalArgumentException("Color cannot be empty"));

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Color cannot be empty"));
    }

    @Test
    void updateFaculty_WithInternalServerError_ShouldReturnInternalServerError() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(eq(1L), any(Faculty.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    @Test
    void deleteFaculty_WithInternalServerError_ShouldReturnInternalServerError() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyService.deleteFaculty(1L))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(INTERNAL_SERVER_ERROR_MESSAGE));
    }

    // Вспомогательные методы
    private Faculty createFaculty(Long id, String name, String color) {
        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);
        return faculty;
    }

    private FacultyDTO createFacultyDTO(Long id, String name, String color) {
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(id);
        facultyDTO.setName(name);
        facultyDTO.setColor(color);
        return facultyDTO;
    }

    private SimpleFacultyDTO createSimpleFacultyDTO(Long id, String name, String color) {
        return new SimpleFacultyDTO(id, name, color);
    }

    private Student createStudent(Long id, String name, int age) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        return student;
    }
}