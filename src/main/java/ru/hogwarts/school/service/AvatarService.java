package ru.hogwarts.school.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.exception.AvatarProcessingException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Transactional
public class AvatarService {
    private final AvatarRepository avatarRepository;
    private final StudentService studentService;
    Logger logger = LoggerFactory.getLogger(AvatarService.class);
    @Value("${avatars.directory.path}")
    private String avatarsDir;

    @Autowired
    public AvatarService(AvatarRepository avatarRepository, StudentService studentService) {
        this.avatarRepository = avatarRepository;
        this.studentService = studentService;
    }

    public Avatar findAvatar(long studentId) {
        logger.info("Was invoked method for get avatar");
        try {
            return avatarRepository.findByStudentId(studentId).orElseThrow();
        } catch (Exception e) {
            logger.error("Error finding avatar for student id: {}", studentId, e);
            throw new AvatarNotFoundException("Error finding avatar for student id: " + studentId, e);
        }
    }

    public List<Avatar> getAllAvatars(Integer pageNumber, Integer pageSize) {
        logger.info("Was invoked method for get all avatars");
        try {
            if (pageNumber == null || pageNumber < 1) {
                throw new IllegalArgumentException("Page number must be greater than 0");
            }
            if (pageSize == null || pageSize < 1) {
                throw new IllegalArgumentException("Page size must be greater than 0");
            }

            PageRequest pageRequest = PageRequest.of(pageNumber - 1, pageSize);
            return avatarRepository.findAll(pageRequest).getContent();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid pagination parameters: pageNumber={}, pageSize={}", pageNumber, pageSize, e);
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving avatars list", e);
            throw new AvatarProcessingException("Error retrieving avatars list", e);
        }
    }

    public void uploadAvatar(Long studentId, MultipartFile file) throws IOException {
        logger.info("Was invoked method for upload avatar");

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (studentId == null || studentId <= 0) {
            throw new IllegalArgumentException("Invalid student id: " + studentId);
        }

        try {
            Student student = studentService.getStudentById(studentId);
            if (student == null) {
                throw new StudentNotFoundException("Student not found with id: " + studentId);
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.trim().isEmpty()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }

            Path filePath = Path.of(avatarsDir, studentId + "." + getExtension(originalFilename));
            Files.createDirectories(filePath.getParent());
            Files.deleteIfExists(filePath);

            try (InputStream is = file.getInputStream();
                 OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
                 BufferedInputStream bis = new BufferedInputStream(is, 1024);
                 BufferedOutputStream bos = new BufferedOutputStream(os, 1024)) {
                bis.transferTo(bos);
            }

            Avatar avatar = avatarRepository.findByStudentId(studentId).orElseGet(Avatar::new);
            avatar.setStudent(student);
            avatar.setFilePath(filePath.toString());
            avatar.setFileSize(file.getSize());
            avatar.setMediaType(file.getContentType());
            avatar.setData(file.getBytes());

            avatarRepository.save(avatar);

        } catch (StudentNotFoundException e) {
            logger.error("Student not found for avatar upload: {}", studentId, e);
            throw e;
        } catch (IOException e) {
            logger.error("IO error during avatar upload for student id: {}", studentId, e);
            throw new AvatarProcessingException("Error processing avatar file for student id: " + studentId, e);
        } catch (Exception e) {
            logger.error("Unexpected error during avatar upload for student id: {}", studentId, e);
            throw new AvatarProcessingException("Unexpected error during avatar upload", e);
        }
    }

    private String getExtension(String fileName) {
        logger.info("Was invoked method for get extension");
        try {
            if (fileName == null || fileName.lastIndexOf(".") == -1) {
                throw new IllegalArgumentException("File name must contain extension: " + fileName);
            }
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        } catch (Exception e) {
            logger.error("Error extracting file extension from: {}", fileName, e);
            throw new IllegalArgumentException("Invalid file name: " + fileName, e);
        }
    }
}
