package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.exception.StudentProcessingException;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Service
@Transactional
public class StudentService {
    Logger logger = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        logger.info("Was invoked method for create student entity");
        try {
            if (student == null) {
                throw new IllegalArgumentException("Student cannot be null");
            }
            if (student.getName() == null || student.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Student name cannot be null or empty");
            }
            if (student.getAge() <= 0) {
                throw new IllegalArgumentException("Student age must be positive");
            }

            return studentRepository.save(student);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during student creation", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error creating student", e);
            throw new StudentProcessingException("Error creating student", e);
        }
    }

    public Student getStudentById(long id) {
        logger.info("Was invoked method for get student by id");
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid student id: " + id);
            }

            return studentRepository.findById(id)
                    .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));
        } catch (StudentNotFoundException e) {
            logger.warn("Student not found with id: {}", id);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving student with id: {}", id, e);
            throw new StudentProcessingException("Error retrieving student with id: " + id, e);
        }
    }

    public Student updateStudent(long id, Student student) {
        logger.info("Was invoked method for update student");
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid student id: " + id);
            }
            if (student == null) {
                throw new IllegalArgumentException("Student cannot be null");
            }

            if (!studentRepository.existsById(id)) {
                throw new StudentNotFoundException("Student not found with id: " + id);
            }

            student.setId(id);
            return studentRepository.save(student);
        } catch (StudentNotFoundException | IllegalArgumentException e) {
            logger.error("Error during student update for id: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during student update for id: {}", id, e);
            throw new StudentProcessingException("Error updating student with id: " + id, e);
        }
    }

    public Student deleteStudent(long id) {
        logger.info("Was invoked method for delete student");
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid student id: " + id);
            }

            Student student = studentRepository.findById(id)
                    .orElseThrow(() -> new StudentNotFoundException("Student not found with id: " + id));

            studentRepository.deleteById(id);
            return student;
        } catch (StudentNotFoundException e) {
            logger.warn("Student not found for deletion with id: {}", id);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id for deletion: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting student with id: {}", id, e);
            throw new StudentProcessingException("Error deleting student with id: " + id, e);
        }
    }

    public Collection<Student> getStudentsByAge(int age) {
        logger.info("Was invoked method for get students by age");
        try {
            if (age <= 0) {
                throw new IllegalArgumentException("Age must be positive");
            }

            return studentRepository.findByAge(age);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid age parameter: {}", age, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving students by age: {}", age, e);
            throw new StudentProcessingException("Error retrieving students by age: " + age, e);
        }
    }

    public Collection<Student> getStudentsByAgeBetween(int min, int max) {
        logger.info("Was invoked method for get students by age between");
        try {
            if (min <= 0 || max <= 0) {
                throw new IllegalArgumentException("Age values must be positive");
            }
            if (min > max) {
                throw new IllegalArgumentException("Min age cannot be greater than max age");
            }

            return studentRepository.findByAgeBetween(min, max);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid age range parameters: min={}, max={}", min, max, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving students by age range: {} - {}", min, max, e);
            throw new StudentProcessingException("Error retrieving students by age range: " + min + " - " + max, e);
        }
    }

    public Collection<Student> getStudentsByFacultyId(int facultyId) {
        logger.info("Was invoked method for get students by faculty id");
        try {
            if (facultyId <= 0) {
                throw new IllegalArgumentException("Invalid faculty id: " + facultyId);
            }

            return studentRepository.findByFacultyId(facultyId);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid faculty id: {}", facultyId, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving students by faculty id: {}", facultyId, e);
            throw new StudentProcessingException("Error retrieving students by faculty id: " + facultyId, e);
        }
    }

    public Integer getCountOfStudents() {
        logger.info("Was invoked method for get count of students");
        try {
            return studentRepository.getCountOfStudents();
        } catch (Exception e) {
            logger.error("Error retrieving student count", e);
            throw new StudentProcessingException("Error retrieving student count", e);
        }
    }

    public Float getAvgAgeOfStudents() {
        logger.info("Was invoked method for get average age of students");
        try {
            return studentRepository.getAvgAgeOfStudents();
        } catch (Exception e) {
            logger.error("Error calculating average age of students", e);
            throw new StudentProcessingException("Error calculating average age of students", e);
        }
    }

    public Collection<Student> getLastFiveStudents() {
        logger.info("Was invoked method for get last five students");
        try {
            return studentRepository.getLastFiveStudents();
        } catch (Exception e) {
            logger.error("Error retrieving last five students", e);
            throw new StudentProcessingException("Error retrieving last five students", e);
        }
    }

    public Collection<String> getStudentNamesStartingWithA() {
        return studentRepository.findAll().stream()
                .map(Student::getName)
                .filter(name -> name.toUpperCase().startsWith("А"))
                .sorted()
                .map(String::toUpperCase)
                .collect(Collectors.toList());
    }

    public Double getAverageAgeUsingFindAll() {
        Collection<Student> students = studentRepository.findAll();
        return students.stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);
    }

    public Integer calculateOptimizedSum() {
        return IntStream.iterate(1, a -> a + 1)
                .parallel()
                .limit(1_000_000)
                .reduce(0, Integer::sum);
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