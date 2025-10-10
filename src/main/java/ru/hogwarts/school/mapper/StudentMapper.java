package ru.hogwarts.school.mapper;

import org.springframework.stereotype.Component;
import ru.hogwarts.school.dto.StudentDTO;
import ru.hogwarts.school.model.Student;

@Component
public class StudentMapper {

    private final FacultyMapper facultyMapper;

    public StudentMapper(FacultyMapper facultyMapper) {
        this.facultyMapper = facultyMapper;
    }

    public StudentDTO toDTO(Student student) {
        if (student == null) {
            return null;
        }

        StudentDTO dto = new StudentDTO();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setAge(student.getAge());
        dto.setFaculty(facultyMapper.toSimpleDTO(student.getFaculty()));

        return dto;
    }

    public Student toEntity(StudentDTO dto) {
        if (dto == null) {
            return null;
        }

        Student student = new Student();
        student.setId(dto.getId());
        student.setName(dto.getName());
        student.setAge(dto.getAge());
        // Faculty устанавливается через отдельный сервис

        return student;
    }
}