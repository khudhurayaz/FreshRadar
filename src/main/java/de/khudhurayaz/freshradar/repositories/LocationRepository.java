package de.khudhurayaz.freshradar.repositories;

import de.khudhurayaz.freshradar.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Integer> {
    Optional<Location> findByLocation(String name);
    Optional<Location> findByLocationId(Integer id);
    Optional<Location> findById(Integer id);
}
