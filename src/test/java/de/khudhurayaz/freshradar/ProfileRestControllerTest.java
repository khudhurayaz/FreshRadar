package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.api.ProfileRestController;
import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.model.Profile;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.ProfileServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProfileServices profileServices;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileRestController profileRestController;

    private Path testUploadDirectory;

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(profileRestController)
                .build();

        testUploadDirectory = Files.createTempDirectory("freshradar-profile-test-");

        ReflectionTestUtils.setField(
                profileRestController,
                "uploadDir",
                testUploadDirectory.toString()
        );
    }

    @Test
    void editProfile_returns400_whenPrincipalIsMissing() throws Exception {
        mockMvc.perform(multipart("/api/profile/edit/update")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster"))
                .andExpect(status().isBadRequest());

        verify(userRepository, never()).findByEmail(any());
        verify(profileServices, never()).save(any());
    }

    @Test
    void editProfile_returns400WithMessage_whenUserDoesNotExist() throws Exception {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(multipart("/api/profile/edit/update")
                        .principal(() -> "test@example.com")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User wurde nicht gefunden!"));

        verify(userRepository).findByEmail("test@example.com");
        verify(profileServices, never()).save(any());
    }

    @Test
    void editProfile_returns200_whenNewProfileIsSavedWithoutImage() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        Profile savedProfile = new Profile();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(profileServices.findByUserId(1))
                .thenReturn(Optional.empty());
        when(profileServices.save(any(ProfileRequest.class)))
                .thenReturn(Optional.of(savedProfile));

        mockMvc.perform(multipart("/api/profile/edit/update")
                        .principal(() -> "test@example.com")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster")
                        .param("existingProfileImage", "/profile-images/1/altes-bild.png"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        ArgumentCaptor<ProfileRequest> captor =
                ArgumentCaptor.forClass(ProfileRequest.class);

        verify(profileServices).save(captor.capture());

        ProfileRequest savedRequest = captor.getValue();
        assertEquals(user, savedRequest.getUser());
        assertEquals("Ayaz", savedRequest.getFirstname());
        assertEquals("Khudhur", savedRequest.getLastname());
        assertEquals("Informatik", savedRequest.getArea());
        assertEquals("Profilbeschreibung", savedRequest.getInfo());
        assertEquals("Münster", savedRequest.getLocation());
        assertEquals(
                "/profile-images/1/altes-bild.png",
                savedRequest.getProfileImage()
        );
    }

    @Test
    void editProfile_usesExistingProfileId_whenProfileAlreadyExists() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        ProfileRequest existingProfile = new ProfileRequest();
        existingProfile.setId(42);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(profileServices.findByUserId(1))
                .thenReturn(Optional.of(existingProfile));
        when(profileServices.save(any(ProfileRequest.class)))
                .thenReturn(Optional.of(new Profile()));

        mockMvc.perform(multipart("/api/profile/edit/update")
                        .principal(() -> "test@example.com")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster")
                        .param("existingProfileImage", "/profile-images/1/bild.png"))
                .andExpect(status().isOk());

        ArgumentCaptor<ProfileRequest> captor =
                ArgumentCaptor.forClass(ProfileRequest.class);

        verify(profileServices).save(captor.capture());

        assertEquals(42, captor.getValue().getId());
    }

    @Test
    void editProfile_returns400_whenProfileCannotBeSaved() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(profileServices.findByUserId(1))
                .thenReturn(Optional.empty());
        when(profileServices.save(any(ProfileRequest.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(multipart("/api/profile/edit/update")
                        .principal(() -> "test@example.com")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster"))
                .andExpect(status().isBadRequest());

        verify(profileServices).save(any(ProfileRequest.class));
    }

    @Test
    void editProfile_savesPngImageAndStoresImagePath() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        MockMultipartFile logoFile = new MockMultipartFile(
                "logoFile",
                "profilbild.png",
                "image/png",
                "test-image-content".getBytes()
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(profileServices.findByUserId(1))
                .thenReturn(Optional.empty());
        when(profileServices.save(any(ProfileRequest.class)))
                .thenReturn(Optional.of(new Profile()));

        mockMvc.perform(multipart("/api/profile/edit/update")
                        .file(logoFile)
                        .principal(() -> "test@example.com")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster")
                        .param("existingProfileImage", ""))
                .andExpect(status().isOk());

        Path savedFile = testUploadDirectory
                .resolve("1")
                .resolve("profile_1.png");

        assertTrue(Files.exists(savedFile));
        assertEquals(
                "test-image-content",
                Files.readString(savedFile)
        );

        ArgumentCaptor<ProfileRequest> captor =
                ArgumentCaptor.forClass(ProfileRequest.class);

        verify(profileServices).save(captor.capture());

        assertEquals(
                "/profile-images/1/profile_1.png",
                captor.getValue().getProfileImage()
        );
    }

    @Test
    void editProfile_savesJpegImageWithJpgExtension() throws Exception {
        User user = new User();
        user.setId(7);
        user.setEmail("test@example.com");

        MockMultipartFile logoFile = new MockMultipartFile(
                "logoFile",
                "profilbild.jpeg",
                "image/jpeg",
                "jpeg-test-content".getBytes()
        );

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(profileServices.findByUserId(7))
                .thenReturn(Optional.empty());
        when(profileServices.save(any(ProfileRequest.class)))
                .thenReturn(Optional.of(new Profile()));

        mockMvc.perform(multipart("/api/profile/edit/update")
                        .file(logoFile)
                        .principal(() -> "test@example.com")
                        .param("firstname", "Ayaz")
                        .param("lastname", "Khudhur")
                        .param("area", "Informatik")
                        .param("info", "Profilbeschreibung")
                        .param("location", "Münster"))
                .andExpect(status().isOk());

        Path savedFile = testUploadDirectory
                .resolve("7")
                .resolve("profile_7.jpg");

        assertTrue(Files.exists(savedFile));

        ArgumentCaptor<ProfileRequest> captor =
                ArgumentCaptor.forClass(ProfileRequest.class);

        verify(profileServices).save(captor.capture());

        assertEquals(
                "/profile-images/7/profile_7.jpg",
                captor.getValue().getProfileImage()
        );
    }
}