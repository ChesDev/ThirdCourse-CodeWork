package ru.hogwarts.school.mapper;

import org.springframework.stereotype.Component;
import ru.hogwarts.school.dto.FacultyDTO;
import ru.hogwarts.school.dto.SimpleFacultyDTO;
import ru.hogwarts.school.dto.SimpleStudentDTO;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FacultyMapper {

    public FacultyDTO toDTO(Faculty faculty) {
        if (faculty == null) {
            return null;
        }

        FacultyDTO dto = new FacultyDTO();
        dto.setId(faculty.getId());
        dto.setName(faculty.getName());
        dto.setColor(faculty.getColor());

        if (faculty.getStudents() != null) {
            List<SimpleStudentDTO> studentDTOs = faculty.getStudents().stream().map(this::toSimpleStudentDTO).collect(Collectors.toList());
            dto.setStudents(studentDTOs);
        }

        return dto;
    }

    public SimpleFacultyDTO toSimpleDTO(Faculty faculty) {
        if (faculty == null) {
            return null;
        }

        return new SimpleFacultyDTO(faculty.getId(), faculty.getName(), faculty.getColor());
    }

    public Faculty toEntity(FacultyDTO dto) {
        if (dto == null) {
            return null;
        }

        Faculty faculty = new Faculty();
        faculty.setId(dto.getId());
        faculty.setName(dto.getName());
        faculty.setColor(dto.getColor());

        return faculty;
    }

    private SimpleStudentDTO toSimpleStudentDTO(Student student) {
        if (student == null) {
            return null;
        }

        return new SimpleStudentDTO(student.getId(), student.getName(), student.getAge());
    }
}