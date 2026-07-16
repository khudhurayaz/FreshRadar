package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.subscription.SubscriptionRequest;
import de.khudhurayaz.freshradar.model.Subscription;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.SubscriptionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository repository;
    public Subscription createSubscription(SubscriptionRequest request, User user) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setStatus(request.getStatus());
        subscription.setPlanType(request.getPlanType());
        subscription.setPurchasedAt(request.getPurchasedAt());
        return repository.save(subscription);
    }

    public Optional<Subscription> save(SubscriptionRequest request) {
        if (request.getId() == 0) {
            return Optional.empty();
        }

        return repository.findById(request.getId())
                .map(subscription -> {
                    subscription.setStatus(request.getStatus());

                    if (request.getPlanType() != null) {
                        subscription.setPlanType(request.getPlanType());
                    }
                    if (request.getPurchasedAt() != null) {
                        subscription.setPurchasedAt(request.getPurchasedAt());
                    }
                    return repository.save(subscription);
                });
    }

    public boolean deleteByUser_Id(int userId) {
        return repository.deleteByUser_Id(userId) > 0;
    }
}
