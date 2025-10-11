package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.StudentDTO;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private StudentMapper studentMapper;

    @MockitoBean
    private FacultyMapper facultyMapper;

    @Test
    void createStudent_ShouldReturnCreatedStudent() throws Exception {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("Гарри Поттер");
        studentDTO.setAge(17);

        Student student = new Student();
        student.setId(1L);
        student.setName("Гарри Поттер");
        student.setAge(17);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.createStudent(any(Student.class))).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Гарри Поттер"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    void getStudent_WhenStudentExists_ShouldReturnStudent() throws Exception {
        // Given
        Student student = new Student();
        student.setId(1L);
        student.setName("Гермиона Грейнджер");
        student.setAge(17);

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("Гермиона Грейнджер");
        studentDTO.setAge(17);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Гермиона Грейнджер"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    void getStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/student/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentFaculty_WhenStudentAndFacultyExist_ShouldReturnFaculty() throws Exception {
        // Given
        Student student = new Student();
        student.setId(1L);
        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName("Гриффиндор");
        faculty.setColor("Красный");
        student.setFaculty(faculty);

        SimpleFacultyDTO facultyDTO = new SimpleFacultyDTO(1L, "Гриффиндор", "Красный");

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/student/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Гриффиндор"))
                .andExpect(jsonPath("$.color").value("Красный"));
    }

    @Test
    void getStudentFaculty_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/student/faculty/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentsByAge_ShouldReturnStudentsList() throws Exception {
        // Given
        Student student = new Student();
        student.setId(1L);
        student.setName("Рон Уизли");
        student.setAge(17);

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("Рон Уизли");
        studentDTO.setAge(17);

        when(studentService.getStudentsByAge(17)).thenReturn(List.of(student));
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/age/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Рон Уизли"))
                .andExpect(jsonPath("$[0].age").value(17));
    }

    @Test
    void getStudentsByAgeBetween_ShouldReturnStudentsList() throws Exception {
        // Given
        Student student = new Student();
        student.setId(1L);
        student.setName("Драко Малфой");
        student.setAge(16);

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("Драко Малфой");
        studentDTO.setAge(16);

        when(studentService.getStudentsByAgeBetween(15, 18)).thenReturn(List.of(student));
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/agebetween/15-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Драко Малфой"))
                .andExpect(jsonPath("$[0].age").value(16));
    }

    @Test
    void updateStudent_WhenStudentExists_ShouldReturnUpdatedStudent() throws Exception {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("Гарри Поттер Updated");
        studentDTO.setAge(18);

        Student student = new Student();
        student.setId(1L);
        student.setName("Гарри Поттер Updated");
        student.setAge(18);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Гарри Поттер Updated"))
                .andExpect(jsonPath("$.age").value(18));
    }

    @Test
    void updateStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(999L);
        studentDTO.setName("Несуществующий студент");
        studentDTO.setAge(20);

        Student student = new Student();
        student.setId(999L);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_WhenStudentExists_ShouldReturnDeletedStudent() throws Exception {
        // Given
        Student student = new Student();
        student.setId(1L);
        student.setName("Невилл Долгопупс");
        student.setAge(17);

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(1L);
        studentDTO.setName("Невилл Долгопупс");
        studentDTO.setAge(17);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(studentService.deleteStudent(1L)).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Невилл Долгопупс"))
                .andExpect(jsonPath("$.age").value(17));
    }

    @Test
    void deleteStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/student/999"))
                .andExpect(status().isNotFound());
    }
}