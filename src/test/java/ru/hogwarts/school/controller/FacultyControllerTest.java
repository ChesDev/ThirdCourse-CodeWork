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

@WebMvcTest(FacultyController.class)
class FacultyControllerTest {

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

    @MockitoBean
    private StudentMapper studentMapper;

    @Test
    void createFaculty_ShouldReturnCreatedFaculty() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, "Гриффиндор", "Красный");
        Faculty faculty = createFaculty(1L, "Гриффиндор", "Красный");

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$.name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$.color").value(facultyDTO.getColor()));
    }

    @Test
    void getFacultyById_WhenFacultyExists_ShouldReturnFaculty() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, "Слизерин", "Зеленый");
        FacultyDTO facultyDTO = createFacultyDTO(1L, "Слизерин", "Зеленый");

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$.name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$.color").value(facultyDTO.getColor()));
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
        Faculty faculty = createFaculty(1L, "Гриффиндор", "Красный");
        Student student = createStudent(1L, "Гарри Поттер", 17);
        SimpleStudentDTO studentDTO = createSimpleStudentDTO(1L, "Гарри Поттер", 17);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(studentService.getStudentsByFacultyId(1L)).thenReturn(List.of(student));
        when(studentMapper.toSimpleDTO(student)).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/faculty/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(studentDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(studentDTO.getName()))
                .andExpect(jsonPath("$[0].age").value(studentDTO.getAge()));
    }

    @Test
    void getFacultyStudents_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/faculty/students/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFacultiesByColorOrName_WhenNameProvided_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, "Когтевран", "Синий");
        FacultyDTO facultyDTO = createFacultyDTO(1L, "Когтевран", "Синий");

        when(facultyService.getFacultiesByName(faculty.getName())).thenReturn(List.of(faculty));
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("name", faculty.getName()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$[0].color").value(facultyDTO.getColor()));
    }

    @Test
    void getFacultiesByColorOrName_WhenColorProvided_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, "Пуффендуй", "Желтый");
        FacultyDTO facultyDTO = createFacultyDTO(1L, "Пуффендуй", "Желтый");

        when(facultyService.getFacultiesByColor(faculty.getColor())).thenReturn(List.of(faculty));
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("color", faculty.getColor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$[0].color").value(facultyDTO.getColor()));
    }

    @Test
    void getFacultiesByColorOrName_WhenNoParams_ShouldReturnAllFaculties() throws Exception {
        // Given
        Faculty faculty1 = createFaculty(1L, "Гриффиндор", "Красный");
        Faculty faculty2 = createFaculty(2L, "Слизерин", "Зеленый");
        FacultyDTO facultyDTO1 = createFacultyDTO(1L, "Гриффиндор", "Красный");
        FacultyDTO facultyDTO2 = createFacultyDTO(2L, "Слизерин", "Зеленый");

        when(facultyService.getAllFaculties()).thenReturn(List.of(faculty1, faculty2));
        when(facultyMapper.toDTO(faculty1)).thenReturn(facultyDTO1);
        when(facultyMapper.toDTO(faculty2)).thenReturn(facultyDTO2);

        // When & Then
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(facultyDTO1.getId()))
                .andExpect(jsonPath("$[0].name").value(facultyDTO1.getName()))
                .andExpect(jsonPath("$[0].color").value(facultyDTO1.getColor()))
                .andExpect(jsonPath("$[1].id").value(facultyDTO2.getId()))
                .andExpect(jsonPath("$[1].name").value(facultyDTO2.getName()))
                .andExpect(jsonPath("$[1].color").value(facultyDTO2.getColor()));
    }

    @Test
    void updateFaculty_WhenFacultyExists_ShouldReturnUpdatedFaculty() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(1L, "Гриффиндор Updated", "Алый");
        Faculty faculty = createFaculty(1L, "Гриффиндор Updated", "Алый");

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$.name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$.color").value(facultyDTO.getColor()));
    }

    @Test
    void updateFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        FacultyDTO facultyDTO = createFacultyDTO(999L, "Несуществующий факультет", "Черный");
        Faculty faculty = createFaculty(999L, "Несуществующий факультет", "Черный");

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
        Faculty faculty = createFaculty(1L, "Гриффиндор", "Красный");
        SimpleFacultyDTO facultyDTO = createSimpleFacultyDTO(1L, "Гриффиндор", "Красный");

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);
        when(facultyService.deleteFaculty(1L)).thenReturn(faculty);

        // When & Then
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$.name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$.color").value(facultyDTO.getColor()));
    }

    @Test
    void deleteFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(facultyService.getFacultyById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/faculty/999"))
                .andExpect(status().isNotFound());
    }

    // Вспомогательные методы для создания объектов
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

    private SimpleStudentDTO createSimpleStudentDTO(Long id, String name, int age) {
        return new SimpleStudentDTO(id, name, age);
    }
}
