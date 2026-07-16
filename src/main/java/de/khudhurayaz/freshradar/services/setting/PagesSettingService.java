package de.khudhurayaz.freshradar.services.setting;

import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.repositories.setting.PagesSettingRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class PagesSettingService {

    private UserRepository userRepository;
    private PagesSettingRepository pagesSettingRepository;

    public PagesSetting savePagesSetting(int userId, int profileSize, int productSize, int contactSize) {
        User user = userRepository.findById(userId).get();
        PagesSetting setting = pagesSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    PagesSetting s = new PagesSetting();
                    s.setUser(user);
                    return s;
                });

        setting.setProfilePageSize(profileSize);
        setting.setProductPageSize(productSize);
        setting.setContactPageSize(contactSize);

        return pagesSettingRepository.save(setting);
    }

    public PagesSetting savePagesSetting(int userId, int userSize) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));

        PagesSetting setting = pagesSettingRepository.findByUser(user)
                .orElseGet(() -> {
                    PagesSetting s = new PagesSetting();
                    s.setUser(user);
                    return s;
                });

        setting.setUserPageSize(userSize);

        return pagesSettingRepository.save(setting);
    }

    public Optional<PagesSetting> findById(int id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden"));
        return pagesSettingRepository.findByUser(user);
    }

    public PagesSetting getPagesSetting(int userID){
        return findById(userID).orElse(null);
    }
}
