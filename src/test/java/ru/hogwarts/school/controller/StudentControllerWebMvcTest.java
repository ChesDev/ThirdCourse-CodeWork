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
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.mapper.FacultyMapper;
import ru.hogwarts.school.mapper.StudentMapper;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.hogwarts.school.controller.TestConstants.*;

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
        StudentDTO studentDTO = createStudentDTO(1L, HARRY_POTTER_NAME, STUDENT_AGE_17);
        Student student = createStudent(1L, HARRY_POTTER_NAME, STUDENT_AGE_17);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.createStudent(any(Student.class))).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(HARRY_POTTER_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE_17));
    }

    @Test
    void createStudent_WithValidationError_ShouldReturnBadRequest() throws Exception {
        // Given
        StudentDTO studentDTO = createStudentDTO(null, "", -5);

        when(studentMapper.toEntity(any(StudentDTO.class)))
                .thenThrow(new IllegalArgumentException("Name cannot be empty"));

        // When & Then
        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStudent_WhenStudentExists_ShouldReturnStudent() throws Exception {
        // Given
        Student student = createStudent(1L, HERMIONE_GRANGER_NAME, STUDENT_AGE_17);
        StudentDTO studentDTO = createStudentDTO(1L, HERMIONE_GRANGER_NAME, STUDENT_AGE_17);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(HERMIONE_GRANGER_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE_17));
    }

    @Test
    void getStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(NON_EXISTENT_ID))
                .thenThrow(new StudentNotFoundException(STUDENT_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(get("/student/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentFaculty_WhenStudentAndFacultyExist_ShouldReturnFaculty() throws Exception {
        // Given
        Faculty faculty = createFaculty(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);
        Student student = createStudentWithFaculty(1L, HARRY_POTTER_NAME, STUDENT_AGE_17, faculty);
        SimpleFacultyDTO facultyDTO = createSimpleFacultyDTO(1L, GRYFFINDOR_NAME, GRYFFINDOR_COLOR);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(facultyMapper.toSimpleDTO(any(Faculty.class))).thenReturn(facultyDTO);

        // When & Then
        mockMvc.perform(get("/student/faculty/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(GRYFFINDOR_NAME))
                .andExpect(jsonPath("$.color").value(GRYFFINDOR_COLOR));
    }

    @Test
    void getStudentFaculty_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(NON_EXISTENT_ID))
                .thenThrow(new StudentNotFoundException(STUDENT_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(get("/student/faculty/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentFaculty_WhenStudentHasNoFaculty_ShouldReturnNoContent() throws Exception {
        // Given
        Student student = createStudent(1L, HARRY_POTTER_NAME, STUDENT_AGE_17);
        student.setFaculty(null);

        when(studentService.getStudentById(1L)).thenReturn(student);

        // When & Then
        mockMvc.perform(get("/student/faculty/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void getCountOfStudents_ShouldReturnCount() throws Exception {
        // Given
        when(studentService.getCountOfStudents()).thenReturn(5);

        // When & Then
        mockMvc.perform(get("/student/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void getAvgAgeOfStudents_ShouldReturnAverageAge() throws Exception {
        // Given
        when(studentService.getAvgAgeOfStudents()).thenReturn(16.5f);

        // When & Then
        mockMvc.perform(get("/student/age/avg"))
                .andExpect(status().isOk())
                .andExpect(content().string("16.5"));
    }

    @Test
    void getLastFiveStudents_ShouldReturnStudents() throws Exception {
        // Given
        Student student = createStudent(1L, RON_WEASLEY_NAME, STUDENT_AGE_17);
        List<Student> students = List.of(student);

        when(studentService.getLastFiveStudents()).thenReturn(students);

        // When & Then
        mockMvc.perform(get("/student/last"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getStudentsByAge_ShouldReturnStudentsList() throws Exception {
        // Given
        Student student = createStudent(1L, RON_WEASLEY_NAME, STUDENT_AGE_17);
        StudentDTO studentDTO = createStudentDTO(1L, RON_WEASLEY_NAME, STUDENT_AGE_17);

        when(studentService.getStudentsByAge(STUDENT_AGE_17)).thenReturn(List.of(student));
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/age/" + STUDENT_AGE_17))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(RON_WEASLEY_NAME))
                .andExpect(jsonPath("$[0].age").value(STUDENT_AGE_17));
    }

    @Test
    void getStudentsByAgeBetween_ShouldReturnStudentsList() throws Exception {
        // Given
        Student student = createStudent(1L, DRACO_MALFOY_NAME, STUDENT_AGE_16);
        StudentDTO studentDTO = createStudentDTO(1L, DRACO_MALFOY_NAME, STUDENT_AGE_16);

        when(studentService.getStudentsByAgeBetween(MIN_AGE, MAX_AGE)).thenReturn(List.of(student));
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(get("/student/agebetween/" + MIN_AGE + "-" + MAX_AGE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(DRACO_MALFOY_NAME))
                .andExpect(jsonPath("$[0].age").value(STUDENT_AGE_16));
    }

    @Test
    void updateStudent_WhenStudentExists_ShouldReturnUpdatedStudent() throws Exception {
        // Given
        String updatedName = HARRY_POTTER_NAME + " Updated";
        int updatedAge = STUDENT_AGE_18;

        StudentDTO studentDTO = createStudentDTO(1L, updatedName, updatedAge);
        Student student = createStudent(1L, updatedName, updatedAge);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.updateStudent(eq(1L), any(Student.class))).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(updatedName))
                .andExpect(jsonPath("$.age").value(updatedAge));
    }

    @Test
    void updateStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        StudentDTO studentDTO = createStudentDTO(NON_EXISTENT_ID, "Несуществующий студент", 20);
        Student student = createStudent(NON_EXISTENT_ID, "Несуществующий студент", 20);

        when(studentMapper.toEntity(any(StudentDTO.class))).thenReturn(student);
        when(studentService.updateStudent(eq(NON_EXISTENT_ID), any(Student.class)))
                .thenThrow(new StudentNotFoundException(STUDENT_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(put("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_WhenStudentExists_ShouldReturnDeletedStudent() throws Exception {
        // Given
        Student student = createStudent(1L, NEVILLE_LONGBOTTOM_NAME, STUDENT_AGE_17);
        StudentDTO studentDTO = createStudentDTO(1L, NEVILLE_LONGBOTTOM_NAME, STUDENT_AGE_17);

        when(studentService.getStudentById(1L)).thenReturn(student);
        when(studentService.deleteStudent(1L)).thenReturn(student);
        when(studentMapper.toDTO(any(Student.class))).thenReturn(studentDTO);

        // When & Then
        mockMvc.perform(delete("/student/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(NEVILLE_LONGBOTTOM_NAME))
                .andExpect(jsonPath("$.age").value(STUDENT_AGE_17));
    }

    @Test
    void deleteStudent_WhenStudentNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(studentService.getStudentById(NON_EXISTENT_ID))
                .thenThrow(new StudentNotFoundException(STUDENT_NOT_FOUND_MESSAGE));

        // When & Then
        mockMvc.perform(delete("/student/" + NON_EXISTENT_ID))
                .andExpect(status().isNotFound());
    }

    // Вспомогательные методы
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