package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.api.ContactRestController;
import de.khudhurayaz.freshradar.dto.ContactRequest;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import de.khudhurayaz.freshradar.services.ContactService;
import de.khudhurayaz.freshradar.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContactRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactService contactService;

    /*
     * Wird von CheckInterceptor benötigt, der über WebConfig geladen wird.
     */
    @MockitoBean
    private UserService userService;

    /*
     * Wird von globalModelAttributes benötigt.
     */
    @MockitoBean
    private UserRepository userRepository;

    @Test
    void getAllContacts_returns200AndMessages_whenMessagesExist() throws Exception {
        ContactRequest firstMessage = new ContactRequest();
        firstMessage.setContactId(1);
        firstMessage.setFirstname("Ayaz");
        firstMessage.setLastname("Khudhur");
        firstMessage.setEmail("ayaz@example.com");
        firstMessage.setSubject("Frage zu FreshRadar");
        firstMessage.setMessage("Hallo, ich habe eine Frage.");
        firstMessage.setContactDate(
                Timestamp.valueOf("2026-07-16 12:00:00")
        );

        ContactRequest secondMessage = new ContactRequest();
        secondMessage.setContactId(2);
        secondMessage.setFirstname("Max");
        secondMessage.setLastname("Mustermann");
        secondMessage.setEmail("max@example.com");
        secondMessage.setSubject("Feedback");
        secondMessage.setMessage("Bitte um Rückmeldung.");
        secondMessage.setContactDate(
                Timestamp.valueOf("2026-07-16 13:00:00")
        );

        when(contactService.findAll())
                .thenReturn(List.of(firstMessage, secondMessage));

        mockMvc.perform(get("/api/contact/allMessages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contactId").value(1))
                .andExpect(jsonPath("$[0].firstname").value("Ayaz"))
                .andExpect(jsonPath("$[0].lastname").value("Khudhur"))
                .andExpect(jsonPath("$[0].email").value("ayaz@example.com"))
                .andExpect(jsonPath("$[0].subject").value("Frage zu FreshRadar"))
                .andExpect(jsonPath("$[0].message").value("Hallo, ich habe eine Frage."))
                .andExpect(jsonPath("$[1].contactId").value(2))
                .andExpect(jsonPath("$[1].firstname").value("Max"))
                .andExpect(jsonPath("$[1].lastname").value("Mustermann"))
                .andExpect(jsonPath("$[1].email").value("max@example.com"))
                .andExpect(jsonPath("$[1].subject").value("Feedback"))
                .andExpect(jsonPath("$[1].message").value("Bitte um Rückmeldung."));

        verify(contactService).findAll();
    }

    @Test
    void getAllContacts_returns400_whenNoMessagesExist() throws Exception {
        when(contactService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/contact/allMessages"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));

        verify(contactService).findAll();
    }

    @Test
    void addContact_returns200_whenMessageIsSaved() throws Exception {
        when(contactService.save(any(ContactRequest.class)))
                .thenReturn(true);

        mockMvc.perform(post("/api/contact/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstname": "Ayaz",
                                  "lastname": "Khudhur",
                                  "email": "ayaz@example.com",
                                  "subject": "Frage zu FreshRadar",
                                  "message": "Hallo, ich habe eine Frage."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Nachricht wurde erfolgreich gesendet!"));

        verify(contactService).save(any(ContactRequest.class));
    }

    @Test
    void addContact_returns400_whenMessageCannotBeSaved() throws Exception {
        when(contactService.save(any(ContactRequest.class)))
                .thenReturn(false);

        mockMvc.perform(post("/api/contact/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstname": "Ayaz",
                                  "lastname": "Khudhur",
                                  "email": "ayaz@example.com",
                                  "subject": "Frage zu FreshRadar",
                                  "message": "Hallo, ich habe eine Frage."
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nachricht konnte nicht gesendet werden!"));

        verify(contactService).save(any(ContactRequest.class));
    }
}