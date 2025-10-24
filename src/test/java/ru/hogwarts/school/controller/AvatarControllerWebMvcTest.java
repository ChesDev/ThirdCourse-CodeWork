package ru.hogwarts.school.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.exception.AvatarProcessingException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AvatarController.class)
class AvatarControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AvatarService avatarService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadAvatar_Success() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/student/1/avatar")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("Avatar uploaded successfully"));
    }

    @Test
    void uploadAvatar_FileTooLarge() throws Exception {
        // Given
        byte[] largeFile = new byte[1024 * 400]; // 400KB
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                largeFile
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/student/1/avatar")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("File is too big")));
    }

    @Test
    void uploadAvatar_StudentNotFound() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test".getBytes()
        );

        Mockito.doThrow(new StudentNotFoundException("Student not found"))
                .when(avatarService).uploadAvatar(anyLong(), any());

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/student/999/avatar")
                        .file(file))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Student not found")));
    }

    @Test
    void uploadAvatar_EmptyFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "avatar",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.multipart("/student/1/avatar")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Avatar file cannot be empty")));
    }

    @Test
    void downloadAvatarPreview_Success() throws Exception {
        // Given
        Avatar avatar = createAvatar(1L, "image/jpeg", "test data".getBytes());
        Mockito.when(avatarService.findAvatar(1L)).thenReturn(avatar);

        // When & Then
        mockMvc.perform(get("/student/1/avatar/preview"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(header().string("Content-Length", "9"));
    }

    @Test
    void downloadAvatarPreview_NotFound() throws Exception {
        // Given
        Mockito.when(avatarService.findAvatar(999L))
                .thenThrow(new AvatarNotFoundException("Avatar not found"));

        // When & Then
        mockMvc.perform(get("/student/999/avatar/preview"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadAvatarFile_Success() throws Exception {
        // Given
        Avatar avatar = createAvatar(1L, "image/jpeg", "test data".getBytes());
        avatar.setFilePath("ru/hogwarts/school/controller/test/avatar.jpg");
        avatar.setFileSize(100L);

        Path tempDir = Files.createTempDirectory("avatar-test");
        Path avatarFile = tempDir.resolve("avatar.jpg");
        Files.write(avatarFile, "test avatar content".getBytes());
        avatar.setFilePath(avatarFile.toString());

        Mockito.when(avatarService.findAvatar(1L)).thenReturn(avatar);

        // When & Then
        mockMvc.perform(get("/student/1/avatar"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        // Удаляем временные файлы после теста
        Files.deleteIfExists(avatarFile);
        Files.deleteIfExists(tempDir);
    }

    @Test
    void downloadAvatarFile_FileNotFoundOnDisk() throws Exception {
        // Given
        Avatar avatar = createAvatar(1L, "image/jpeg", "test data".getBytes());
        avatar.setFilePath("/nonexistent/path/avatar.jpg");
        avatar.setFileSize(100L);

        Mockito.when(avatarService.findAvatar(1L)).thenReturn(avatar);

        // When & Then
        mockMvc.perform(get("/student/1/avatar"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllAvatars_Success() throws Exception {
        // Given
        Avatar avatar1 = createAvatar(1L, "image/jpeg", "data1".getBytes());
        Avatar avatar2 = createAvatar(2L, "image/png", "data2".getBytes());
        List<Avatar> avatars = List.of(avatar1, avatar2);

        Mockito.when(avatarService.getAllAvatars(1, 10)).thenReturn(avatars);

        // When & Then
        mockMvc.perform(get("/student/avatar/all")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    void getAllAvatars_InvalidPagination() throws Exception {
        // Given
        Mockito.when(avatarService.getAllAvatars(0, 10))
                .thenThrow(new IllegalArgumentException("Page number must be greater than 0"));

        // When & Then
        mockMvc.perform(get("/student/avatar/all")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllAvatars_MissingParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/student/avatar/all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllAvatars_ProcessingException() throws Exception {
        // Given
        Mockito.when(avatarService.getAllAvatars(1, 10))
                .thenThrow(new AvatarProcessingException("Ошибка обработки аватаров"));

        // When & Then
        mockMvc.perform(get("/student/avatar/all")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    // Вспомогательные методы
    private Avatar createAvatar(Long id, String mediaType, byte[] data) {
        Avatar avatar = new Avatar();
        avatar.setId(id);
        avatar.setMediaType(mediaType);
        avatar.setData(data);
        return avatar;
    }
}