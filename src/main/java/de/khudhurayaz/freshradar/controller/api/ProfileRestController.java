package de.khudhurayaz.freshradar.controller.api;

import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.model.Profile;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.ProfileServices;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Optional;

@Log4j2
@Controller
@RequestMapping("/api/profile/edit")
public class ProfileRestController {

    @Autowired
    private ProfileServices profileServices;
    @Autowired
    private UserRepository userRepository;

    @Value("${app.upload.dir:./uploads/profile/}")
    private String uploadDir;

    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editProfile(
            Principal principal,
            @RequestParam String firstname,
            @RequestParam String lastname,
            @RequestParam String area,
            @RequestParam String info,
            @RequestParam String location,
            @RequestParam(required = false, defaultValue = "") String existingProfileImage,
            @RequestParam(required = false) MultipartFile logoFile
    ) {
        if (principal == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> userOptional = userRepository.findByEmail(principal.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("User wurde nicht gefunden!");
        }

        User user = userOptional.get();
        log.debug("Aktualisiere Profil für '{}'", user.getEmail());

        String imagePath = existingProfileImage;

        ProfileRequest profileRequest = new ProfileRequest();
        if (logoFile != null && !logoFile.isEmpty()) {
            String filename = saveImage(logoFile, user.getId());
            imagePath = "/profile-images/" + user.getId() + "/" + filename;
            log.debug("Speichere neues Profilbild in DB: {}", imagePath);
            profileRequest.setProfileImage(imagePath);
        } else if (imagePath != null) {
            profileRequest.setProfileImage(imagePath);
        }

        profileRequest.setUser(user);
        profileRequest.setFirstname(firstname);
        profileRequest.setLastname(lastname);
        profileRequest.setArea(area);
        profileRequest.setInfo(info);
        profileRequest.setLocation(location);

        Optional<ProfileRequest> existingProfile = profileServices.findByUserId(user.getId());
        existingProfile.ifPresent(profile -> profileRequest.setId(profile.getId()));

        Optional<Profile> savedProfile = profileServices.save(profileRequest);

        if (savedProfile.isPresent()) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

    private String saveImage(MultipartFile file, int userId) {
        try {
            String contentType = file.getContentType();
            String extension = "png";

            if ("image/jpeg".equalsIgnoreCase(contentType)) {
                extension = "jpg";
            } else if ("image/png".equalsIgnoreCase(contentType)) {
                extension = "png";
            }

            Path userUploadDir = Paths.get(uploadDir, String.valueOf(userId)).toAbsolutePath().normalize();
            Files.createDirectories(userUploadDir);

            String filename = "profile_" + userId + "." + extension;
            Path filePath = userUploadDir.resolve(filename);

            Files.write(filePath, file.getBytes());

            log.debug("Profilbild gespeichert: {}", filePath);
            if (Files.exists(filePath)) {
                log.debug("Bild existiert direkt nach dem Speichern.");
            }

            return filename;
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Speichern des Bildes", e);
        }
    }
}