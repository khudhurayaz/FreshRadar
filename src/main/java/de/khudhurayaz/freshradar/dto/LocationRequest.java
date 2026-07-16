package de.khudhurayaz.freshradar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LocationRequest {
    private int id;
    private String location;
    private Timestamp addedAt;
}
