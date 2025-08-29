package ru.hogwarts.school.service;

import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Student;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final Map<Long, Student> studentMap = new HashMap<>();
    private long generatedId = 0L;

    public Student createStudent(Student student) {
        student.setId(generatedId);
        studentMap.put(generatedId, student);
        generatedId++;
        return student;
    }

    public Student getStudentById(long id) {
        return studentMap.get(id);
    }

    public Student updateStudent(long id, Student student) {
        studentMap.put(id,student);
        return student;
    }

    public Student deleteStudent(long id) {
        return studentMap.remove(id);
    }

    public List<Student> getStudentsByAge(int age) {
        return studentMap.values().stream()
                .filter(student -> student.getAge() == age)
                .collect(Collectors.toList());
    }
}