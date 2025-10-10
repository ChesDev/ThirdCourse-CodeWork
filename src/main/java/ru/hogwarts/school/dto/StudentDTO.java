package ru.hogwarts.school.dto;

public class StudentDTO {
    private Long id;
    private String name;
    private int age;
    private SimpleFacultyDTO faculty;

    public StudentDTO() {
    }

    public StudentDTO(Long id, String name, int age, SimpleFacultyDTO faculty) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.faculty = faculty;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public SimpleFacultyDTO getFaculty() {
        return faculty;
    }

    public void setFaculty(SimpleFacultyDTO faculty) {
        this.faculty = faculty;
    }
}