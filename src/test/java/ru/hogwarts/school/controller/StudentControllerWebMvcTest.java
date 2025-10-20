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
class StudentControllerWebMvcTest {

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
        StudentDTO studentDTO = createStudentDTO(1L, "Гарри Поттер", 17);
        Student student = createStudent(1L, "Гарри Поттер", 17);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.createStudent(any(Student.class))).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentDTO.getId()))
                .andExpect(jsonPath("$.name").value(studentDTO.getName()))
                .andExpect(jsonPath("$.age").value(studentDTO.getAge()));
    }

    @Test
    void getStudent_WhenStudentExists_ShouldReturnStudent() throws Exception {
        // Given
        Student student = createStudent(1L, "Гермиона Грейнджер", 17);
        StudentDTO studentDTO = createStudentDTO(1L, "Гермиона Грейнджер", 17);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentDTO.getId()))
                .andExpect(jsonPath("$.name").value(studentDTO.getName()))
                .andExpect(jsonPath("$.age").value(studentDTO.getAge()));
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
        Faculty faculty = createFaculty(1L, "Гриффиндор", "Красный");
        Student student = createStudentWithFaculty(1L, "Гарри Поттер", 17, faculty);
        SimpleFacultyDTO facultyDTO = createSimpleFacultyDTO(1L, "Гриффиндор", "Красный");

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/student/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(facultyDTO.getId()))
                .andExpect(jsonPath("$.name").value(facultyDTO.getName()))
                .andExpect(jsonPath("$.color").value(facultyDTO.getColor()));
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
        Student student = createStudent(1L, "Рон Уизли", 17);
        StudentDTO studentDTO = createStudentDTO(1L, "Рон Уизли", 17);

        when(studentService.getStudentsByAge(17)).thenReturn(List.of(student));
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/age/17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(studentDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(studentDTO.getName()))
                .andExpect(jsonPath("$[0].age").value(studentDTO.getAge()));
    }

    @Test
    void getStudentsByAgeBetween_ShouldReturnStudentsList() throws Exception {
        // Given
        Student student = createStudent(1L, "Драко Малфой", 16);
        StudentDTO studentDTO = createStudentDTO(1L, "Драко Малфой", 16);

        when(studentService.getStudentsByAgeBetween(15, 18)).thenReturn(List.of(student));
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/agebetween/15-18"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(studentDTO.getId()))
                .andExpect(jsonPath("$[0].name").value(studentDTO.getName()))
                .andExpect(jsonPath("$[0].age").value(studentDTO.getAge()));
    }

    @Test
    void updateStudent_WhenStudentExists_ShouldReturnUpdatedStudent() throws Exception {
        // Given
        StudentDTO studentDTO = createStudentDTO(1L, "Гарри Поттер Updated", 18);
        Student student = createStudent(1L, "Гарри Поттер Updated", 18);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.updateStudent(anyLong(), any(Student.class))).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentDTO.getId()))
                .andExpect(jsonPath("$.name").value(studentDTO.getName()))
                .andExpect(jsonPath("$.age").value(studentDTO.getAge()));
    }

    @Test
    void updateStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        StudentDTO studentDTO = createStudentDTO(999L, "Несуществующий студент", 20);
        Student student = createStudent(999L, "Несуществующий студент", 20);

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
        Student student = createStudent(1L, "Невилл Долгопупс", 17);
        StudentDTO studentDTO = createStudentDTO(1L, "Невилл Долгопупс", 17);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(studentService.deleteStudent(1L)).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(studentDTO.getId()))
                .andExpect(jsonPath("$.name").value(studentDTO.getName()))
                .andExpect(jsonPath("$.age").value(studentDTO.getAge()));
    }

    @Test
    void deleteStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(999L)).thenReturn(null);

        // When & Then
        mockMvc.perform(delete("/student/999"))
                .andExpect(status().isNotFound());
    }

    // Вспомогательные методы для создания объектов
    private Student createStudent(Long id, String name, int age) {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        return student;
    }

    private Student createStudentWithFaculty(Long id, String name, int age, Faculty faculty) {
        Student student = createStudent(id, name, age);
        student.setFaculty(faculty);
        return student;
    }

    private StudentDTO createStudentDTO(Long id, String name, int age) {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setId(id);
        studentDTO.setName(name);
        studentDTO.setAge(age);
        return studentDTO;
    }

    private Faculty createFaculty(Long id, String name, String color) {
        Faculty faculty = new Faculty();
        faculty.setId(id);
        faculty.setName(name);
        faculty.setColor(color);
        return faculty;
    }

    private SimpleFacultyDTO createSimpleFacultyDTO(Long id, String name, String color) {
        return new SimpleFacultyDTO(id, name, color);
    }
}
