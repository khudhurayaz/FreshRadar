package de.khudhurayaz.freshradar.controller.api;

import de.khudhurayaz.freshradar.dto.inventory.CreateInventoryRequest;
import de.khudhurayaz.freshradar.dto.inventory.InventoryRequest;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.dto.inventory.UpdateInventoryRequest;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.services.InventoryService;
import de.khudhurayaz.freshradar.services.ProductService;
import de.khudhurayaz.freshradar.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
@Log4j2
public class InventoryRestController {

    private final InventoryService inventoryService;
    private final UserService userService;
    private final ProductService productService;


    /**
     * Mit dieser Endpunkt, kann man alle inventories welches dem aktuellen benutzer gehört, anzeigen lassen.
     * @param principal Wer ist derzeit eingeloggt!
     * @return Eine liste von InventoryRequest Klasse.
     */
    @GetMapping("/showInventories")
    public List<InventoryRequest> showInventories(Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());
        return inventoryService.findAll(user.get().getId());
    }

    /**
     * Mit dieser Endpunkt kann man einen neuen Inventor hinzufügen.
     * @param inventoryRequest Ein parameter von der Klasse CreateInventoryRequest wird erwartet.
     * @return Rückgabewert ist ein String
     */
    @PutMapping("/addInventory")
    public ResponseEntity<String> addInventory(
            @RequestBody CreateInventoryRequest inventoryRequest) {
        log.debug("Adding inventory to database");

        CreateInventoryRequest request = inventoryService.addInventory(inventoryRequest);
        log.debug("Adding inventory[{}]", request.toString());
        return request != null ? ResponseEntity.ok().body("Inventar wurde erfolgreich hinzugefügt.") :
                                 ResponseEntity.badRequest().body("Es konnte kein neues Inventar hinzugefügt werden.");
    }

    /**
     * Mit dieser Endpunkt kann man einen bestehendes Inventory löschen.
     * @param id Es benötigt lediglich die InventoryID.
     * @return Ein Ok für erfolgreich gelöscht als String oder ein NOT_FOUND mit Inventory konnte nicht gelöscht werden als Nachricht!
     */
    @DeleteMapping("/deleteInventory/{id}")
    public ResponseEntity<String> deleteInventory(@PathVariable int id) {
        if (inventoryService.delete(id)) {
            return ResponseEntity.ok("Inventory wurde erfolgreich gelöscht!");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Inventory konnte nicht gelöscht werden!");
    }

    /**
     * Mit dieser Endpunkt kann man sein Inventar ändern.
     * @param request Es erwartet eine CreateInventoryRequest parameter.
     * @return Ein String wird ausgegeben, OK für Erfolg und NOT_FOUND für konnte nicht aktualisiert werden.
     */
    @PutMapping("/updateInventory")
    public ResponseEntity<String> updateInventory(
            @RequestBody UpdateInventoryRequest request) {
        if (inventoryService.save(request)) {
            log.debug("Inventar wurde erfolgreich aktualisiert!");
            return ResponseEntity.ok("Inventar wurde erfolgreich aktualisiert!");
        }

        log.debug("Inventar konnte nicht aktualisiert werden!");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                "Inventar konnte nicht aktualisiert werden!"
        );
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<InventoryRequest> getInventory(
            @PathVariable int productId,
            Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());
        Optional<ProductRequest> product = productService.findByProductId(productId);
        Optional<InventoryRequest> inventoryRequest =  Optional.empty();
        if (user.isPresent() && product.isPresent()) {
            inventoryRequest =  inventoryService.findByProductId(product.get().getId());
        }
        return inventoryRequest.map(request -> new ResponseEntity<>(request, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/inventory/healthCareCheckup")
    public ResponseEntity<String> healthCareCheckup(Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());

        if (user.isEmpty()) {
            return new ResponseEntity<>("[Inventar] Benutzer nicht gefunden!", HttpStatus.UNAUTHORIZED);
        }

        List<InventoryRequest> inventoryRequestList = inventoryService.findAll(user.get().getId());

        if (inventoryRequestList == null || inventoryRequestList.isEmpty()) {
            return new ResponseEntity<>("[Inventar] Keine Einträge wurden gefunden!", HttpStatus.NOT_FOUND);
        }

        long countUnderSupplied = inventoryRequestList.stream()
                .filter(item -> item.getQuantity() > 0) // Division durch 0 verhindern
                .filter(item -> {
                    // Umwandlung in double für korrekte Prozentrechnung
                    double prozent = ((double) item.getCurrentQuantity() / item.getQuantity()) * 100;
                    return prozent < 20.0;
                })
                .count();

        if (countUnderSupplied > 0) {
            return new ResponseEntity<>("[Inventar] Unterversorgt! Bitte nachkaufen!", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>("[Inventar] Bestens versorgt! Der Vorrat ist ausreichend.", HttpStatus.OK);
        }
    }
}
