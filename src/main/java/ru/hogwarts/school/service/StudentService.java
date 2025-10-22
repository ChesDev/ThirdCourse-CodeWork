package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.Optional;


@Service
@Transactional
public class StudentService {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudentById(long id) {
        Optional<Student> student = studentRepository.findById(id);
        return student.orElse(null);
    }

    public Student updateStudent(long id, Student student) {
        if (studentRepository.existsById(id)) {
            student.setId(id);
            return studentRepository.save(student);
        }
        return null;
    }

    public Student deleteStudent(long id) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isPresent()) {
            studentRepository.deleteById(id);
            return student.get();
        }
        return null;
    }

    public Collection<Student> getStudentsByAge(int age) {
        return studentRepository.findByAge(age);
    }

    public Collection<Student> getStudentsByAgeBetween(int min, int max) {
        return studentRepository.findByAgeBetween(min, max);
    }

    public Collection<Student> getStudentsByFacultyId(int facultyId) {
        return studentRepository.findByFacultyId(facultyId);
    }

    public Integer getCountOfStudents() {
        return studentRepository.getCountOfStudents();
    }

    public Float getAvgAgeOfStudents() {
        return studentRepository.getAvgAgeOfStudents();
    }

    public Collection<Student> getLastFiveStudents() {
        return studentRepository.getLastFiveStudents();
    }
}