package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.FacultyProcessingException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.Collection;

@Service
public class FacultyService {
    Logger logger = LoggerFactory.getLogger(FacultyService.class);

    private final FacultyRepository facultyRepository;

    @Autowired
    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        logger.info("Was invoked method for create faculty");
        try {
            if (faculty == null) {
                throw new IllegalArgumentException("Faculty cannot be null");
            }
            if (faculty.getName() == null || faculty.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Faculty name cannot be null or empty");
            }
            if (faculty.getColor() == null || faculty.getColor().trim().isEmpty()) {
                throw new IllegalArgumentException("Faculty color cannot be null or empty");
            }

            return facultyRepository.save(faculty);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error during faculty creation", e);
            throw e;
        } catch (Exception e) {
            logger.error("Error creating faculty", e);
            throw new FacultyProcessingException("Error creating faculty", e);
        }
    }

    public Faculty getFacultyById(long id) {
        logger.info("Was invoked method for get faculty by id");
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid faculty id: " + id);
            }

            return facultyRepository.findById(id)
                    .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with id: " + id));
        } catch (FacultyNotFoundException e) {
            logger.warn("Faculty not found with id: {}", id);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid faculty id: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving faculty with id: {}", id, e);
            throw new FacultyProcessingException("Error retrieving faculty with id: " + id, e);
        }
    }

    public Faculty updateFaculty(long id, Faculty faculty) {
        logger.info("Was invoked method for update faculty");
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid faculty id: " + id);
            }
            if (faculty == null) {
                throw new IllegalArgumentException("Faculty cannot be null");
            }

            if (!facultyRepository.existsById(id)) {
                throw new FacultyNotFoundException("Faculty not found with id: " + id);
            }

            faculty.setId(id);
            return facultyRepository.save(faculty);
        } catch (FacultyNotFoundException | IllegalArgumentException e) {
            logger.error("Error during faculty update for id: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during faculty update for id: {}", id, e);
            throw new FacultyProcessingException("Error updating faculty with id: " + id, e);
        }
    }

    public Faculty deleteFaculty(long id) {
        logger.info("Was invoked method for delete faculty");
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Invalid faculty id: " + id);
            }

            Faculty faculty = facultyRepository.findById(id)
                    .orElseThrow(() -> new FacultyNotFoundException("Faculty not found with id: " + id));

            facultyRepository.deleteById(id);
            return faculty;
        } catch (FacultyNotFoundException e) {
            logger.warn("Faculty not found for deletion with id: {}", id);
            throw e;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid faculty id for deletion: {}", id, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting faculty with id: {}", id, e);
            throw new FacultyProcessingException("Error deleting faculty with id: " + id, e);
        }
    }

    public Collection<Faculty> getAllFaculties() {
        logger.info("Was invoked method for get all faculties");
        try {
            return facultyRepository.findAll();
        } catch (Exception e) {
            logger.error("Error retrieving all faculties", e);
            throw new FacultyProcessingException("Error retrieving all faculties", e);
        }
    }

    public Collection<Faculty> getFacultiesByColor(String color) {
        logger.info("Was invoked method for get faculties by color");
        try {
            if (color == null || color.trim().isEmpty()) {
                throw new IllegalArgumentException("Color cannot be null or empty");
            }

            return facultyRepository.findByColorIgnoreCase(color);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid color parameter: {}", color, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving faculties by color: {}", color, e);
            throw new FacultyProcessingException("Error retrieving faculties by color: " + color, e);
        }
    }

    public Collection<Faculty> getFacultiesByName(String name) {
        logger.info("Was invoked method for get faculties by name");
        try {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be null or empty");
            }

            return facultyRepository.findByNameIgnoreCase(name);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid name parameter: {}", name, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving faculties by name: {}", name, e);
            throw new FacultyProcessingException("Error retrieving faculties by name: " + name, e);
        }
    }
}