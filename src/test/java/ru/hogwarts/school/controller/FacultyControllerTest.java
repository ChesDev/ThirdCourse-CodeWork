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
        String facultyName = "Гриффиндор";
        String facultyColor = "Красный";

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName(facultyName);
        facultyDTO.setColor(facultyColor);

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyName);
        faculty.setColor(facultyColor);

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.createFaculty(any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(post("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(facultyName))
                .andExpect(jsonPath("$.color").value(facultyColor));
    }

    @Test
    void getFacultyById_WhenFacultyExists_ShouldReturnFaculty() throws Exception {
        // Given
        String facultyName = "Слизерин";
        String facultyColor = "Зеленый";

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyName);
        faculty.setColor(facultyColor);

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName(facultyName);
        facultyDTO.setColor(facultyColor);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(facultyName))
                .andExpect(jsonPath("$.color").value(facultyColor));
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
        String facultyName = "Гриффиндор";
        String studentName = "Гарри Поттер";

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyName);

        Student student = new Student();
        student.setId(1L);
        student.setName(studentName);
        student.setAge(17);

        SimpleStudentDTO studentDTO = new SimpleStudentDTO(1L, studentName, 17);

        when(facultyService.getFacultyById(1)).thenReturn(faculty);
        when(studentService.getStudentsByFacultyId(1)).thenReturn(List.of(student));

        // When & Then
        mockMvc.perform(get("/faculty/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value(studentName))
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
        String facultyName = "Когтевран";
        String facultyColor = "Синий";

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyName);
        faculty.setColor(facultyColor);

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName(facultyName);
        facultyDTO.setColor(facultyColor);

        when(facultyService.getFacultiesByName(facultyName)).thenReturn(List.of(faculty));
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("name", facultyName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value(facultyName))
                .andExpect(jsonPath("$[0].color").value(facultyColor));
    }

    @Test
    void getFacultiesByColorOrName_WhenColorProvided_ShouldReturnFilteredFaculties() throws Exception {
        // Given
        String facultyName = "Пуффендуй";
        String facultyColor = "Желтый";

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyName);
        faculty.setColor(facultyColor);

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName(facultyName);
        facultyDTO.setColor(facultyColor);

        when(facultyService.getFacultiesByColor(facultyColor)).thenReturn(List.of(faculty));
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/faculty")
                        .param("color", facultyColor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value(facultyName))
                .andExpect(jsonPath("$[0].color").value(facultyColor));
    }

    @Test
    void getFacultiesByColorOrName_WhenNoParams_ShouldReturnAllFaculties() throws Exception {
        // Given
        String facultyName1 = "Гриффиндор";
        String facultyName2 = "Слизерин";
        String facultyColor1 = "Красный";
        String facultyColor2 = "Зеленый";

        Faculty faculty1 = new Faculty();
        faculty1.setId(1L);
        faculty1.setName(facultyName1);
        faculty1.setColor(facultyColor1);

        Faculty faculty2 = new Faculty();
        faculty2.setId(2L);
        faculty2.setName(facultyName2);
        faculty2.setColor(facultyColor2);

        FacultyDTO facultyDTO1 = new FacultyDTO();
        facultyDTO1.setId(1L);
        facultyDTO1.setName(facultyName1);
        facultyDTO1.setColor(facultyColor1);

        FacultyDTO facultyDTO2 = new FacultyDTO();
        facultyDTO2.setId(2L);
        facultyDTO2.setName(facultyName2);
        facultyDTO2.setColor(facultyColor2);

        when(facultyService.getAllFaculties()).thenReturn(List.of(faculty1, faculty2));
        when(facultyMapper.toDTO(faculty1)).thenReturn(facultyDTO1);
        when(facultyMapper.toDTO(faculty2)).thenReturn(facultyDTO2);

        // When & Then
        mockMvc.perform(get("/faculty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value(facultyName1))
                .andExpect(jsonPath("$[0].color").value(facultyColor1))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value(facultyName2))
                .andExpect(jsonPath("$[1].color").value(facultyColor2));
    }

    @Test
    void updateFaculty_WhenFacultyExists_ShouldReturnUpdatedFaculty() throws Exception {
        // Given
        String updatedFacultyName = "Гриффиндор Updated";
        String updatedFacultycolor = "Алый";

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(1L);
        facultyDTO.setName(updatedFacultyName);
        facultyDTO.setColor(updatedFacultyColor);

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(updatedFacultyName);
        faculty.setColor(updatedFacultyColor);

        when(facultyMapper.toEntity(any(FacultyDTO.class))).thenReturn(faculty);
        when(facultyService.updateFaculty(anyLong(), any(Faculty.class))).thenReturn(faculty);
        when(facultyMapper.toDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(put("/faculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(updatedFacultyName))
                .andExpect(jsonPath("$.color").value(updatedFacultyColor));
    }

    @Test
    void updateFaculty_WhenFacultyNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        String nonExistFacultyName = "Несуществующий факультет";
        String nonExistFacultyColor ≠ "Черный";

        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setId(999L);
        facultyDTO.setName(nonExistFacultyName);
        facultyDTO.setColor(nonExistFacultyColor);

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
        String facultyName = "Гриффиндор";
        String facultyColor = "Красный";

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyName);
        faculty.setColor(facultyColor);

        SimpleFacultyDTO facultyDTO = new SimpleFacultyDTO(1L, facultyName, facultyColor);

        when(facultyService.getFacultyById(1L)).thenReturn(faculty);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);
        when(facultyService.deleteFaculty(1L)).thenReturn(faculty);

        // When & Then
        mockMvc.perform(delete("/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(facultyName))
                .andExpect(jsonPath("$.color").value(facultyColor));
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