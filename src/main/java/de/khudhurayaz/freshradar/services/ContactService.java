package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.ContactRequest;
import de.khudhurayaz.freshradar.model.Contact;
import de.khudhurayaz.freshradar.repositories.ContactRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log4j2
public class ContactService {
    private final ContactRepository  contactRepository;

    public List<ContactRequest> findAll(){
        return contactRepository.findAll().stream()
                .map(this::setContactRequest).toList();
    }

    public boolean save(ContactRequest request) {
        Contact contact = new Contact();
        contact.setFirstName(request.getFirstname());
        contact.setLastName(request.getLastname());
        contact.setEmail(request.getEmail());
        contact.setSubject(request.getSubject());
        contact.setMessage(request.getMessage());
        contact.setContactDate(request.getContactDate());
        contactRepository.save(contact);
        return true;
    }

    private ContactRequest setContactRequest(Contact cr) {
        ContactRequest contactRequest = new ContactRequest();
        contactRequest.setContactId(cr.getContactId());
        contactRequest.setFirstname(cr.getFirstName());
        contactRequest.setLastname(cr.getLastName());
        contactRequest.setEmail(cr.getEmail());
        contactRequest.setSubject(cr.getSubject());
        contactRequest.setMessage(cr.getMessage());
        contactRequest.setContactDate(cr.getContactDate());
        return contactRequest;
    }

    public Optional<ContactRequest> findById(int messageId) {
        ContactRequest request = setContactRequest(contactRepository.findByContactId(messageId).get());
       return Optional.of(request);
    }

    public Optional<Boolean> delete(ContactRequest contactRequest) {
        return contactRepository.deleteByContactId(contactRequest.getContactId());
    }
}
