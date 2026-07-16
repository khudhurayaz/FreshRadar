package de.khudhurayaz.freshradar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequest {
    private Integer contactId;
    private String firstname;
    private String lastname;
    private String email;
    private String subject;
    private String message;
    private Timestamp contactDate;
}
