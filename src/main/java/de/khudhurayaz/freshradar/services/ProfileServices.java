package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.ProfileRequest;
import de.khudhurayaz.freshradar.model.Profile;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.ProfileRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ProfileServices {

    private UserService userService;
    private ProfileRepository repository;
    public Optional<ProfileRequest> findByUserId(int userId) {
        return repository.findByUserId(userId)
                .map(this::toRequest);
    }
    public Optional<ProfileRequest> findById(int profileId) {
        return repository.findById(profileId).map(this::toRequest);
    }
    public Optional<ProfileRequest> findByUserEmail(String userEmail) {
        Optional<User> user = userService.findByEmail(userEmail);
        if (user.isPresent()) {
            if (user.get().getProfile() == null) {
                return Optional.empty();
            }
            ProfileRequest profileRequest = new ProfileRequest();
            user.ifPresent(profileRequest::setUser);
            ProfileRequest request = new ProfileRequest();
            request.setId(user.get().getProfile().getId());
            request.setUser(user.get());
            request.setFirstname(user.get().getProfile().getFirstname());
            request.setLastname(user.get().getProfile().getLastname());
            request.setArea(user.get().getProfile().getArea());
            request.setInfo(user.get().getProfile().getInfo());
            request.setLocation(user.get().getProfile().getLocation());
            request.setProfileImage(user.get().getProfile().getProfileImage());
            return Optional.of(request);
        }
        return Optional.empty();
    }

    public Optional<List<ProfileRequest>> getAll(){
        List<ProfileRequest> requests = new ArrayList<>();
        List<Profile> profiles = repository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        for (Profile p : profiles) {
            ProfileRequest request = new ProfileRequest();
            request.setId(p.getId());
            request.setUser(p.getUser());
            request.setFirstname(p.getFirstname());
            request.setLastname(p.getLastname());
            request.setArea(p.getArea());
            request.setInfo(p.getInfo());
            request.setLocation(p.getLocation());
            request.setProfileImage(p.getProfileImage());
            requests.add(request);
        }
        return Optional.of(requests);
    }
    private ProfileRequest toRequest(Profile profile) {
        ProfileRequest request = new ProfileRequest();
        request.setId(profile.getId());
        request.setFirstname(profile.getFirstname());
        request.setLastname(profile.getLastname());
        request.setArea(profile.getArea());
        request.setInfo(profile.getInfo());
        request.setLocation(profile.getLocation());
        request.setProfileImage(profile.getProfileImage());
        request.setUser(profile.getUser());
        return request;
    }

    public Optional<Profile> save(ProfileRequest request) {
        Profile profile = repository.findByUserId(request.getUser().getId())
                .orElse(new Profile());

        if (request.getUser() != null)
            profile.setUser(request.getUser());
        if (request.getFirstname() != null)
            profile.setFirstname(request.getFirstname());
        if (request.getLastname() != null)
            profile.setLastname(request.getLastname());
        if (request.getArea() != null)
            profile.setArea(request.getArea());
        if (request.getInfo() != null)
            profile.setInfo(request.getInfo());
        if (request.getProfileImage() != null)
            profile.setProfileImage(request.getProfileImage());
        if (request.getLocation() != null)
            profile.setLocation(request.getLocation());

        Profile savedProfile = repository.save(profile);
        return Optional.of(savedProfile);
    }

    public boolean delete(int id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
