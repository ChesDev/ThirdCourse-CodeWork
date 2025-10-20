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
class StudentControllerRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/student";
    }

    @Test
    void createStudent() {
        String studentName = "Гарри Поттер";
        int studentAge = 17;

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(studentName);
        studentDTO.setAge(studentAge);

        ResponseEntity<StudentDTO> response = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(studentName, response.getBody().getName());
        assertEquals(studentAge, response.getBody().getAge());
    }

    @Test
    void getStudent() {
        // Сначала создаем студента
        String studentName = "Гермиона Грейнджер";
        int studentAge = 17;

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(studentName);
        studentDTO.setAge(studentAge);

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        Long studentId = createResponse.getBody().getId();

        // Затем получаем его
        ResponseEntity<StudentDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId, StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(studentName, response.getBody().getName());
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
        String studentName = "Рон Уизли";
        int studentAge = 17;

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(studentName);
        studentDTO.setAge(studentAge);

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
        String studentName = "Драко Малфой";
        int studentAge = 17;

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(studentName);
        studentDTO.setAge(studentAge);

        restTemplate.postForEntity(getBaseUrl(), studentDTO, StudentDTO.class);

        ResponseEntity<StudentDTO[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/age/" + studentAge, StudentDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getStudentsByAgeBetween() {
        // Создаем студентов разных возрастов
        String student1Name = "Невилл Лонгботтом";
        int student1Age = 16;
        String student2Name = "Луна Лавгуд";
        int student2Age = 17;
        int minAge = 16;
        int maxAge = 18;

        StudentDTO student1 = new StudentDTO();
        student1.setName(student1Name);
        student1.setAge(student1Age);

        StudentDTO student2 = new StudentDTO();
        student2.setName(student2Name);
        student2.setAge(student2Age);

        restTemplate.postForEntity(getBaseUrl(), student1, StudentDTO.class);
        restTemplate.postForEntity(getBaseUrl(), student2, StudentDTO.class);

        ResponseEntity<StudentDTO[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/agebetween/" + minAge + "-" + maxAge, StudentDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void updateStudent() {
        // Создаем студента
        String oldName = "Седрик Диггори";
        int oldAge = 17;
        String newName = "Седрик Диггори (обновленный)";
        int newAge = 18;

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(oldName);
        studentDTO.setAge(oldAge);

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        StudentDTO createdStudent = createResponse.getBody();

        // Обновляем студента
        createdStudent.setName(newName);
        createdStudent.setAge(newAge);

        ResponseEntity<StudentDTO> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.PUT,
                new HttpEntity<>(createdStudent), StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(newName, response.getBody().getName());
        assertEquals(newAge, response.getBody().getAge());
    }

    @Test
    void deleteStudent() {
        // Создаем студента
        String studentName = "Фред Уизли";
        int studentAge = 18;

        StudentDTO studentDTO = new StudentDTO();
        studentDTO.setName(studentName);
        studentDTO.setAge(studentAge);

        ResponseEntity<StudentDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), studentDTO, StudentDTO.class);
        Long studentId = createResponse.getBody().getId();

        // Удаляем студента
        ResponseEntity<StudentDTO> response = restTemplate.exchange(
                getBaseUrl() + "/" + studentId, HttpMethod.DELETE,
                null, StudentDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(studentName, response.getBody().getName());

        // Проверяем, что студент удален
        ResponseEntity<StudentDTO> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + studentId, StudentDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}
