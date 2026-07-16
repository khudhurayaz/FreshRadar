package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.CreateLocationRequest;
import de.khudhurayaz.freshradar.dto.LocationRequest;
import de.khudhurayaz.freshradar.model.Location;
import de.khudhurayaz.freshradar.repositories.LocationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    public Optional<LocationRequest> getLocation(int locationId) {
        return convertFormLocationToLocationRequest(locationRepository.findByLocationId(locationId));
    }
    public Optional<Location> getLocation(String name) {
        return locationRepository.findByLocation(name);
    }
    public Optional<Location> findById(int locationId) {
        return locationRepository.findById(locationId);
    }

    public boolean save(CreateLocationRequest request, int locationId) {
        return locationRepository.findById(locationId)
                .map(location -> {
                    location.setLocation(request.getLocation());
                    location.setAddedAt(request.getAdded_at());
                    locationRepository.save(location);
                    return true;
                })
                .orElse(false);
    }

    public boolean delete(int locationId) {
        Optional<Location> location = locationRepository.findById(locationId);
        if (location.isPresent()) {
            locationRepository.delete(location.get());
            return true;
        }
        return false;
    }

    public Optional<Boolean> addLocation(CreateLocationRequest request) {
        Location location = new Location();
        location.setLocation(request.getLocation());
        location.setAddedAt(new Timestamp(System.currentTimeMillis()));
        Location savedLocation = locationRepository.save(location);
        return Optional.of(savedLocation != null);
    }

    public List<LocationRequest> getLocations() {
        return locationRepository.findAll().stream().map(l -> {
            LocationRequest request = new LocationRequest();
            request.setLocation(l.getLocation());
            request.setId(l.getLocationId());
            request.setAddedAt(l.getAddedAt());
            return request;
        }).toList();
    }

    private Optional<LocationRequest> convertFormLocationToLocationRequest(Optional<Location> location) {
        return location.map(value -> new LocationRequest(value.getLocationId(), value.getLocation(), value.getAddedAt()));
    }
}
