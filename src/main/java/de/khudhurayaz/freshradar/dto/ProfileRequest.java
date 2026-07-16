package de.khudhurayaz.freshradar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.khudhurayaz.freshradar.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProfileRequest {
    private int id;
    private User user;
    private String firstname;
    private String lastname;
    private String area;
    private String info;
    private String location;
    private String profileImage;
}
