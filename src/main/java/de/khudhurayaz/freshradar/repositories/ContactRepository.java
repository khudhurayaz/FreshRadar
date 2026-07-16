package de.khudhurayaz.freshradar.repositories;

import de.khudhurayaz.freshradar.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByContactId(int id);
    Optional<Boolean> deleteByContactId(int id);
}
