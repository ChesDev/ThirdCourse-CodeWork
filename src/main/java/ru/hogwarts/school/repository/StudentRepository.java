package ru.hogwarts.school.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.hogwarts.school.model.Student;

import java.util.Collection;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Collection<Student> findByAge(int age);

    Collection<Student> findByAgeBetween(int min, int max);

    Collection<Student> findByFacultyId(int facultyId);

    @Query(value = "SELECT COUNT(*) FROM students", nativeQuery = true)
    Integer getCountOfStudents();

    @Query(value = "SELECT AVG(age) FROM students", nativeQuery = true)
    Float getAvgAgeOfStudents();

    @Query(value = "SELECT * FROM students ORDER BY id DESC LIMIT 5", nativeQuery = true)
    Collection<Student> getLastFiveStudents();
}
