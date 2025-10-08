package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.SimpleStudentDTO;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FacultyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FacultyService facultyService;

    @Mock
    private StudentService studentService;

    @Mock
    private FacultyMapper facultyMapper;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private FacultyController facultyController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(facultyController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createFaculty_ShouldReturnCreatedFaculty() throws Exception {
        // Given
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName("Gryffindor");
        facultyDTO.setColor("Red");

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Red"));
    }

    @Test
    void getFacultyById_WhenFacultyExists_ShouldReturnFaculty() throws Exception {
        // Given
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Slytherin");
        faculty.setColor("Green");

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName("Slytherin");
        facultyDTO.setColor("Green");

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Slytherin"))
                .andExpect(jsonPath("$.color").value("Green"));
    }

    @Test
    void getFacultyById_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFacultyStudents_WhenFacultyExists_ShouldReturnStudentsList() throws Exception {
        // Given
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");

        Student student = new Student();
        student.setId(1L);
        student.setName("Harry Potter");
        student.setAge(17);

        SimpleStudentDTO studentDTO = new SimpleStudentDTO(1L, "Harry Potter", 17);

        when(facultyService.getFacultyById(1)).thenReturn(faculty);
        when(studentService.getStudentsByFacultyId(1)).thenReturn(List.of(student));

        // When & Then
        mockMvc.perform(get("/faculty/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Harry Potter"))
                .andExpect(jsonPath("$[0].age").value(17));
    }

    @Test
    void getFacultyStudents_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(999)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/faculty/students/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFacultiesByColorOrName_WhenNameProvided_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Ravenclaw");
        faculty.setColor("Blue");

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName("Ravenclaw");
        facultyDTO.setColor("Blue");

        when(facultyService.getFacultiesByName("Ravenclaw")).thenReturn(List.of(faculty));
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("name", "Ravenclaw"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Ravenclaw"))
                .andExpect(jsonPath("$[0].color").value("Blue"));
    }

    @Test
    void getFacultiesByColorOrName_WhenColorProvided_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Hufflepuff");
        faculty.setColor("Yellow");

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName("Hufflepuff");
        facultyDTO.setColor("Yellow");

        when(facultyService.getFacultiesByColor("Yellow")).thenReturn(List.of(faculty));
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("color", "Yellow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Hufflepuff"))
                .andExpect(jsonPath("$[0].color").value("Yellow"));
    }

    @Test
    void getFacultiesByColorOrName_WhenNoParams_ShouldReturnAllFaculties() throws Exception {
        // Given
        Faculty faculty1 = new Faculty();
        faculty1.setId(1L);
        faculty1.setName("Gryffindor");
        faculty1.setColor("Red");

        Faculty faculty2 = new Faculty();
        faculty2.setId(2L);
        faculty2.setName("Slytherin");
        faculty2.setColor("Green");

        FacultyDTO facultyDTO1 = new FacultyDTO();
        facultyDTO1.setId(1L);
        facultyDTO1.setName("Gryffindor");
        facultyDTO1.setColor("Red");

        FacultyDTO facultyDTO2 = new FacultyDTO();
        facultyDTO2.setId(2L);
        facultyDTO2.setName("Slytherin");
        facultyDTO2.setColor("Green");

        when(facultyService.getAllFaculties()).thenReturn(List.of(faculty1, faculty2));
        when(facultyMapper.toDTO(faculty1)).thenReturn(facultyDTO1);
        when(facultyMapper.toDTO(faculty2)).thenReturn(facultyDTO2);

        // When & Then
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Gryffindor"))
                .andExpect(jsonPath("$[0].color").value("Red"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("Slytherin"))
                .andExpect(jsonPath("$[1].color").value("Green"));
    }

    @Test
    void updateFaculty_WhenFacultyExists_ShouldReturnUpdatedFaculty() throws Exception {
        // Given
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName("Gryffindor Updated");
        facultyDTO.setColor("Scarlet");

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor Updated");
        faculty.setColor("Scarlet");

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Gryffindor Updated"))
                .andExpect(jsonPath("$.color").value("Scarlet"));
    }

    @Test
    void updateFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(999L);
        facultyDTO.setName("Non-existent Faculty");
        facultyDTO.setColor("Black");

        Faculty faculty = new Faculty();
        faculty.setId(999L);

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteFaculty_WhenFacultyExists_ShouldReturnDeletedFaculty() throws Exception {
        // Given
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Gryffindor");
        faculty.setColor("Red");

        SimpleFacultyDTO facultyDTO = new SimpleFacultyDTO(1L, "Gryffindor", "Red");

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);
        when(facultyService.deleteFaculty(1L)).thenReturn(faculty);

        // When & Then
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Gryffindor"))
                .andExpect(jsonPath("$.color").value("Red"));
    }

    @Test
    void deleteFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/faculty/999"))
                .andExpect(status().isNotFound());
    }
}