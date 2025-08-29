package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Faculty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FacultyService {
    private final Map<Long, Faculty> facultyMap = new HashMap<>();
    private long generatedId = 0L;

    public Faculty createFaculty(Faculty faculty) {
        faculty.setId(generatedId);
        facultyMap.put(generatedId, faculty);
        generatedId++;
        return faculty;
    }

    public Faculty getFacultyById(long id) {
        return facultyMap.get(id);
    }

    public Faculty updateFaculty(long id, Faculty faculty) {
        facultyMap.put(id ,faculty);
        return faculty;
    }

    public Faculty deleteFaculty(long id) {
        return facultyMap.remove(id);
    }

    public List<Faculty> getFacultiesByColor(String color) {
        return facultyMap.values().stream()
                .filter(faculty -> faculty.getColor().equalsIgnoreCase(color))
                .collect(Collectors.toList());
    }
}