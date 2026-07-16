package de.khudhurayaz.freshradar.repositories.setting;

import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagesSettingRepository extends JpaRepository<PagesSetting, Integer> {
    Optional<PagesSetting> findByUser(User user);
}
