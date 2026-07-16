package de.khudhurayaz.freshradar.repositories;

import de.khudhurayaz.freshradar.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    @Transactional
    long deleteByUser_Id(int userId);
}
