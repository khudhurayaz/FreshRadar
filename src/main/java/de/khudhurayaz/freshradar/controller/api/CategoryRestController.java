package de.khudhurayaz.freshradar.controller.api;

import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.services.CategoryService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/category")
@AllArgsConstructor
@Log4j2
public class CategoryRestController {
    private final CategoryService service;

    /**
     * Zeige alle Kategorien, die es in der Datenbank gibt.
     * @return Eine liste von CategoryRequest typ oder es wird ein BadRequest ausgelöst, falls die Liste leer ist.
     */
    @GetMapping("/show")
    public ResponseEntity<List<CategoryRequest>> showAllCategories() {
        List<CategoryRequest> categoryRequests = service.findAll();
        log.debug(categoryRequests.toString());
        return categoryRequests.isEmpty() ? ResponseEntity.badRequest().build() : new ResponseEntity<>(categoryRequests, HttpStatus.OK);
    }

    /**
     * Fügt eine neue Kategorie in die Datenbank zu.
     * @param request Erwartet einen CategoryRequest typ.
     * @return Ein OK wird ausgegeben, falls die Kategorie erfolgreich hinzugefügt wurde oder
     *          einen badRequest.
     */
    @PutMapping("/add")
    public ResponseEntity<String> addCategory(
            @RequestBody CategoryRequest request
    ){
        log.debug("Füge eine neue Kategorie hinzu! {}", request);
        Optional<Boolean> add = service.add(request);
        if (add.isPresent() && add.get()) {
            log.debug("Kategorie erfolgreich gespeichert! '{}'", request.getName());
            return ResponseEntity.ok().body("Kategorie erfolgreich gespeichert!");
        } else {
            log.debug("Kategorie konnte nicht gespeichert werden! '{}'", request.getName());
            return ResponseEntity.badRequest().body("Kategorie konnte nicht gespeichert werden!");
        }
    }
}
