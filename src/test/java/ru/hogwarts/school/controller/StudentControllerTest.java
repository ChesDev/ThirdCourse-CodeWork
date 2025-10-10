package ru.hogwarts.school.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.hogwarts.school.dto.StudentDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StudentControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/student";
    }

    @Test
    void createStudent() {
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName("Гарри Поттер");
        studentDTO.setAge(17);

        ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Гарри Поттер", response.getBody().getName());
        assertEquals(17, response.getBody().getAge());
    }

    @Test
    void getStudent() {
        // Сначала создаем студента
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName("Гермиона Грейнджер");
        studentDTO.setAge(17);

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        Long studentId = createResponse.getBody().getId();

        // Затем получаем его
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId, StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Гермиона Грейнджер", response.getBody().getName());
    }

    @Test
    void getStudentNotFound() {
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/9999", StudentDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getStudentFaculty() {
        // Создаем студента с факультетом
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName("Рон Уизли");
        studentDTO.setAge(17);
        // Предполагаем, что у студента есть facultyId

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        Long studentId = createResponse.getBody().getId();

        ResponseEntity<SimpleFacultyDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/faculty/" + studentId, SimpleFacultyDTO.class);

        // Может вернуть NOT_FOUND или NO_CONTENT в зависимости от наличия факультета
        assertTrue(response.getStatusCode() == HttpStatus.OK ||
                response.getStatusCode() == HttpStatus.NO_CONTENT);
    }

    @Test
    void getStudentsByAge() {
        // Создаем студента определенного возраста
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName("Драко Малфой");
        studentDTO.setAge(17);

        restTemplate.postForEntity(getBaseUrl(), studentDTO, StudentDTO.class);

        ResponseEntity<StudentDTO[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/age/17", StudentDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getStudentsByAgeBetween() {
        // Создаем студентов разных возрастов
        StudentDTO student1 = new StudentDTO();
        student1.setName("Невилл Лонгботтом");
        student1.setAge(16);

        StudentDTO student2 = new StudentDTO();
        student2.setName("Луна Лавгуд");
        student2.setAge(17);

        restTemplate.postForEntity(getBaseUrl(), student1, StudentDTO.class);
        restTemplate.postForEntity(getBaseUrl(), student2, StudentDTO.class);

        ResponseEntity<StudentDTO[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/agebetween/16-18", StudentDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateStudent() {
        // Создаем студента
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName("Седрик Диггори");
        studentDTO.setAge(17);

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        StudentDTO createdStudent = createResponse.getBody();

        // Обновляем студента
        createdStudent.setName("Седрик Диггори (обновленный)");
        createdStudent.setAge(18);

        ResponseEntity<StudentDTO> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.PUT,
                new HttpEntity<>(createdStudent), StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Седрик Диггори (обновленный)", response.getBody().getName());
        assertEquals(18, response.getBody().getAge());
    }

    @Test
    void deleteStudent() {
        // Создаем студента
        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName("Фред Уизли");
        studentDTO.setAge(18);

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        Long studentId = createResponse.getBody().getId();

        // Удаляем студента
        ResponseEntity<StudentDTO> response = restTemplate.exchange(
                getBaseUrl() + "/" + studentId, HttpMethod.DELETE,
                null, StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Фред Уизли", response.getBody().getName());

        // Проверяем, что студент удален
        ResponseEntity<StudentDTO> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId, StudentDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}