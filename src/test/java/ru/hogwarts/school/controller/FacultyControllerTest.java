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
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.SimpleStudentDTO;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FacultyControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String getBaseUrl() {
        return "http://localhost:" + port + "/faculty";
    }

    @Test
    void createFaculty() {
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setName("Гриффиндор");
        facultyDTO.setColor("Красный");

        ResponseEntity<FacultyDTO> response = restTemplate.postForEntity(
                getBaseUrl(), facultyDTO, FacultyDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Гриффиндор", response.getBody().getName());
        assertEquals("Красный", response.getBody().getColor());
    }

    @Test
    void getFacultyById() {
        // Сначала создаем факультет
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setName("Слизерин");
        facultyDTO.setColor("Зеленый");

        ResponseEntity<FacultyDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), facultyDTO, FacultyDTO.class);
        Long facultyId = createResponse.getBody().getId();

        // Затем получаем его
        ResponseEntity<FacultyDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/" + facultyId, FacultyDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Слизерин", response.getBody().getName());
        assertEquals("Зеленый", response.getBody().getColor());
    }

    @Test
    void getFacultyByIdNotFound() {
        ResponseEntity<FacultyDTO> response = restTemplate.getForEntity(
                getBaseUrl() + "/9999", FacultyDTO.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getFacultyStudents() {
        // Создаем факультет
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setName("Когтевран");
        facultyDTO.setColor("Синий");

        ResponseEntity<FacultyDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), facultyDTO, FacultyDTO.class);
        Long facultyId = createResponse.getBody().getId();

        ResponseEntity<SimpleStudentDTO[]> response = restTemplate.getForEntity(
                getBaseUrl() + "/students/" + facultyId, SimpleStudentDTO[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void getFacultiesByColorOrName() {
        // Создаем факультеты для тестирования
        FacultyDTO faculty1 = new FacultyDTO();
        faculty1.setName("Пуффендуй");
        faculty1.setColor("Желтый");

        FacultyDTO faculty2 = new FacultyDTO();
        faculty2.setName("Гриффиндор");
        faculty2.setColor("Красный");

        restTemplate.postForEntity(getBaseUrl(), faculty1, FacultyDTO.class);
        restTemplate.postForEntity(getBaseUrl(), faculty2, FacultyDTO.class);

        // Тестируем поиск по имени
        ResponseEntity<FacultyDTO[]> responseByName = restTemplate.getForEntity(
                getBaseUrl() + "?name=Гриффиндор", FacultyDTO[].class);

        assertEquals(HttpStatus.OK, responseByName.getStatusCode());
        assertNotNull(responseByName.getBody());

        // Тестируем поиск по цвету
        ResponseEntity<FacultyDTO[]> responseByColor = restTemplate.getForEntity(
                getBaseUrl() + "?color=Желтый", FacultyDTO[].class);

        assertEquals(HttpStatus.OK, responseByColor.getStatusCode());
        assertNotNull(responseByColor.getBody());

        // Тестируем получение всех факультетов
        ResponseEntity<FacultyDTO[]> responseAll = restTemplate.getForEntity(
                getBaseUrl(), FacultyDTO[].class);

        assertEquals(HttpStatus.OK, responseAll.getStatusCode());
        assertNotNull(responseAll.getBody());
    }

    @Test
    void updateFaculty() {
        // Создаем факультет
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setName("Старое название");
        facultyDTO.setColor("Старый цвет");

        ResponseEntity<FacultyDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), facultyDTO, FacultyDTO.class);
        FacultyDTO createdFaculty = createResponse.getBody();

        // Обновляем факультет
        createdFaculty.setName("Новое название");
        createdFaculty.setColor("Новый цвет");

        ResponseEntity<FacultyDTO> response = restTemplate.exchange(
                getBaseUrl(), HttpMethod.PUT,
                new HttpEntity<>(createdFaculty), FacultyDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Новое название", response.getBody().getName());
        assertEquals("Новый цвет", response.getBody().getColor());
    }

    @Test
    void deleteFaculty() {
        // Создаем факультет
        FacultyDTO facultyDTO = new FacultyDTO();
        facultyDTO.setName("Факультет для удаления");
        facultyDTO.setColor("Черный");

        ResponseEntity<FacultyDTO> createResponse = restTemplate.postForEntity(
                getBaseUrl(), facultyDTO, FacultyDTO.class);
        Long facultyId = createResponse.getBody().getId();

        // Удаляем факультет
        ResponseEntity<SimpleFacultyDTO> response = restTemplate.exchange(
                getBaseUrl() + "/" + facultyId, HttpMethod.DELETE,
                null, SimpleFacultyDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Факультет для удаления", response.getBody().getName());

        // Проверяем, что факультет удален
        ResponseEntity<FacultyDTO> getResponse = restTemplate.getForEntity(
                getBaseUrl() + "/" + facultyId, FacultyDTO.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}