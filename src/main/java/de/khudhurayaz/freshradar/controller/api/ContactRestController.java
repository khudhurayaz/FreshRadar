package de.khudhurayaz.freshradar.controller.api;

import de.khudhurayaz.freshradar.dto.ContactRequest;
import de.khudhurayaz.freshradar.model.Contact;
import de.khudhurayaz.freshradar.services.ContactService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/contact")
@AllArgsConstructor
@Log4j2
public class ContactRestController {

    private final ContactService contactService;

    /**
     * Alle Nachrichten werden angezeigt, nur ein Admin kann die Nachrichten aufrufen!
     * @return Eine liste von Nachrichten werden zurückgegeben oder ein BadRequest, falls fehler auftauchen.
     */
    @GetMapping("/allMessages")
    public ResponseEntity<List<ContactRequest>> getAllContacts() {
        List<ContactRequest> contactRequests = contactService.findAll();
        return contactRequests.isEmpty() ? ResponseEntity.badRequest().build() : new ResponseEntity<>(contactRequests, HttpStatus.OK);
    }

    /**
     * Hier wird eine Nachricht im Datenbank gespeichert, sobald ein Benutzer auf den Button Nachricht Senden im Kontaktbereich klickt!
     * @param contactRequest Ein ContactRequest von frontend wird erwartet, das wäre als json übergeben.
     * @return Rückgabewert ist ein String, dieser String enthält zwei version je nachdem wie die speicherung ist. OK oder BadRequest.
     */
    @PostMapping("/add")
    public ResponseEntity<String> addContact(
            @RequestBody ContactRequest contactRequest
    ) {
        contactRequest.setContactDate(Timestamp.valueOf(LocalDateTime.now()));
        return contactService.save(contactRequest) ? ResponseEntity.ok().body("Nachricht wurde erfolgreich gesendet!") :
                ResponseEntity.badRequest().body("Nachricht konnte nicht gesendet werden!");
    }

}
