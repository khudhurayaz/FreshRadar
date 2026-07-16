package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class UserService {
    private UserRepository userRepository;
    @Transactional
    public boolean deleteByEmail(String email) {
        return userRepository.deleteByEmail(email) > 0;
    }

    public User findById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void updateLastLogin(String email) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        log.debug("Updating last login at {}", timestamp);
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLoginAt(timestamp);
            userRepository.save(user);
        });
    }
}