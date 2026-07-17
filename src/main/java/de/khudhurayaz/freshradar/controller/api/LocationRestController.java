package de.khudhurayaz.freshradar.controller.api;

import de.khudhurayaz.freshradar.dto.CreateLocationRequest;
import de.khudhurayaz.freshradar.dto.LocationRequest;
import de.khudhurayaz.freshradar.services.LocationService;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/location")
@AllArgsConstructor
@Log4j2
@ToString
public class LocationRestController {
    private LocationService locationService;

    /**
     * @return Eine Liste von allen Lagerorte wird zurückgeliefert.
     */
    @GetMapping("/get")
    public List<LocationRequest> showLocations() {
        return locationService.getLocations();
    }

    /**
     * Ein neuer Lagerort hinzufügen.
     * @param request Der CreateLocationRequest hat nur zwei variablen, welches einmal der Name der Lager ist und der Zeitstempel.
     * @return Als Rückgabewert bekommt der Anwender einen String, in der es drin steht, welches Problem es sich gab oder das Lagerort
     *          Erfolgreich hinzugefügt wurde.
     */
    @PutMapping("/add")
    public ResponseEntity<String> addInventory(
            @RequestBody CreateLocationRequest request) {
        log.debug("Adding location to database");
        Optional<Boolean> add = locationService.addLocation(request);
        if (add.isPresent() && add.get()) {
            log.debug("Lagerort erfolgreich gespeichert! '{}'", request.getLocation());
            return ResponseEntity.ok().body("Lagerort erfolgreich gespeichert!");
        } else {
            log.debug("Lagerort konnte nicht gespeichert werden! '{}'", request.getLocation());
            return ResponseEntity.badRequest().body("Lagerort konnte nicht gespeichert werden!");
        }
    }

    /**
     * Mit dieser Endpunkt kann ein Lagerort aktualisiert werden.
     * @param request Der Klasse CreateLocationRequest wird erwartet.
     * @param id Lagerort ID wird als Parameter übergeben.
     * @return Als Rückgabewert wird zwei Nachrichten ausgaben.
     */
    @PutMapping("/update/{id}")
    public String updateLocation(@RequestBody CreateLocationRequest request, @PathVariable String id) {
        return locationService.save(request, Integer.parseInt(id)) ? "Location konnte nicht aktualisiert werden!" :  "Location wurde erfolgreich aktualisiert!";
    }

    /**
     * Mit dieser Endpunkt kann ein Lagerort wieder gelöscht werden.
     * @param id Lagerort ID wird lediglich benötigt.
     * @return Ein ResponseEntity, ob es erfolgreich oder nicht war.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteLocation(@PathVariable String id){
        if (locationService.delete(Integer.parseInt(id))){
            return ResponseEntity.ok("Location wurde erfolgreich gelöscht!");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Location konnte nicht gelöscht werden!"
        );
    }

    /**
     * Durch dieser Endpunkt kann ein Lagerort mit der jeweiligen ID geholt werden.
     * @param id Lagerort ID wird benötigt.
     * @return Rückgabewert ein ResponseEntity von Type LocationRequest zurückgeliefert.
     */
    @GetMapping("/get/{id}")
    public ResponseEntity<LocationRequest> getLocationById(
            @PathVariable Integer id){
        if (locationService.getLocation(id).isPresent()) {
            return ResponseEntity.ok().body(locationService.getLocation(id).get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
