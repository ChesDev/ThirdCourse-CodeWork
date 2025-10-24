package ru.hogwarts.school.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.exception.AvatarProcessingException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RequestMapping("student")
@RestController
public class AvatarController {
    private static final Logger logger = LoggerFactory.getLogger(AvatarController.class);

    public final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long id, @RequestParam MultipartFile avatar) {
        logger.info("Received request to upload avatar for student id: {}", id);

        try {
            if (avatar == null || avatar.isEmpty()) {
                logger.warn("Attempt to upload empty avatar for student id: {}", id);
                return ResponseEntity.badRequest().body("Avatar file cannot be empty");
            }

            if (avatar.getSize() > 1024 * 300) {
                logger.warn("Avatar file too large for student id: {}, size: {}", id, avatar.getSize());
                return ResponseEntity.badRequest().body("File is too big. Maximum size is 300KB");
            }

            avatarService.uploadAvatar(id, avatar);
            logger.info("Successfully uploaded avatar for student id: {}", id);
            return ResponseEntity.ok("Avatar uploaded successfully");

        } catch (StudentNotFoundException e) {
            logger.error("Student not found for avatar upload: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Student not found with id: " + id);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request parameters for avatar upload: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AvatarProcessingException e) {
            logger.error("Error processing avatar for student id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing avatar: " + e.getMessage());
        } catch (IOException e) {
            logger.error("IO error during avatar upload for student id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("IO error during avatar upload");
        } catch (Exception e) {
            logger.error("Unexpected error during avatar upload for student id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error during avatar upload");
        }
    }

    @GetMapping(value = "/{id}/avatar/preview")
    public ResponseEntity<byte[]> downloadAvatar(@PathVariable Long id) {
        logger.info("Received request to download avatar preview for student id: {}", id);

        try {
            Avatar avatar = avatarService.findAvatar(id);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(avatar.getMediaType()));
            headers.setContentLength(avatar.getData().length);
            headers.setCacheControl("no-cache");

            logger.info("Successfully retrieved avatar preview for student id: {}", id);
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(avatar.getData());

        } catch (AvatarNotFoundException e) {
            logger.warn("Avatar not found for student id: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id for avatar preview: {}", id, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error retrieving avatar preview for student id: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(value = "/{id}/avatar")
    public void downloadAvatar(@PathVariable Long id, HttpServletResponse response) {
        logger.info("Received request to download avatar file for student id: {}", id);

        try {
            Avatar avatar = avatarService.findAvatar(id);
            Path path = Path.of(avatar.getFilePath());

            if (!Files.exists(path)) {
                logger.error("Avatar file not found on disk for student id: {}, path: {}", id, path);
                response.setStatus(HttpStatus.NOT_FOUND.value());
                return;
            }

            try (InputStream is = Files.newInputStream(path);
                 OutputStream os = response.getOutputStream()) {
                response.setStatus(HttpStatus.OK.value());
                response.setContentType(avatar.getMediaType());
                response.setContentLength((int) avatar.getFileSize());
                is.transferTo(os);
                logger.info("Successfully served avatar file for student id: {}", id);
            }

        } catch (AvatarNotFoundException e) {
            logger.warn("Avatar not found for student id: {}", id, e);
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } catch (IllegalArgumentException e) {
            logger.error("Invalid student id for avatar download: {}", id, e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        } catch (IOException e) {
            logger.error("IO error during avatar download for student id: {}", id, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            logger.error("Unexpected error during avatar download for student id: {}", id, e);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @GetMapping(value = "avatar/all")
    public ResponseEntity<?> getAllAvatars(@RequestParam("page") Integer pageNumber,
                                           @RequestParam("size") Integer pageSize) {
        logger.info("Received request to get all avatars, page: {}, size: {}", pageNumber, pageSize);

        try {
            if (pageNumber == null || pageSize == null) {
                return ResponseEntity.badRequest().body("Page number and size are required");
            }

            List<Avatar> avatars = avatarService.getAllAvatars(pageNumber, pageSize);
            logger.info("Successfully retrieved {} avatars", avatars.size());
            return ResponseEntity.ok(avatars);

        } catch (IllegalArgumentException e) {
            logger.error("Invalid pagination parameters: page={}, size={}", pageNumber, pageSize, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (AvatarProcessingException e) {
            logger.error("Error retrieving avatars list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving avatars list");
        } catch (Exception e) {
            logger.error("Unexpected error retrieving avatars list", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error retrieving avatars");
        }
    }
}