package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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

    public Collection<Student> getFirstSixStudents() {
        return studentRepository.findAll().stream()
                .limit(6)
                .collect(Collectors.toList());
    }

    public void printParallel(Collection<Student> students) {
        List<Student> studentList = new ArrayList<>(students);

        // Первые два имени в основном потоке
        System.out.println(studentList.get(0).getName());
        System.out.println(studentList.get(1).getName());

        // Третье и четвертое имя в параллельном потоке
        Thread thread1 = new Thread(() -> {
            System.out.println(studentList.get(2).getName());
            System.out.println(studentList.get(3).getName());
        });

        // Пятое и шестое имя в другом параллельном потоке
        Thread thread2 = new Thread(() -> {
            System.out.println(studentList.get(4).getName());
            System.out.println(studentList.get(5).getName());
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void synchronizedPrint(String name) {
        System.out.println(name);
    }

    public void printSynchronized(Collection<Student> students) {
        List<Student> studentList = new ArrayList<>(students);

        // Первые два имени в основном потоке
        synchronizedPrint(studentList.get(0).getName());
        synchronizedPrint(studentList.get(1).getName());

        // Третье и четвертое имя в параллельном потоке
        Thread thread1 = new Thread(() -> {
            synchronizedPrint(studentList.get(2).getName());
            synchronizedPrint(studentList.get(3).getName());
        });

        // Пятое и шестое имя в другом параллельном потоке
        Thread thread2 = new Thread(() -> {
            synchronizedPrint(studentList.get(4).getName());
            synchronizedPrint(studentList.get(5).getName());
        });

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
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
}