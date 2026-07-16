package de.khudhurayaz.freshradar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {
    private Integer id;
    private Integer userId;
    private Integer locationId;
    private Integer categoryId;
    private String name;
    private Boolean isOpen;
    private String unit;
    private Integer vorratProzent;
    private Integer soll;
    private Integer ist;
    private String vorratFarbe;
    private Timestamp expiryDate;
    private Timestamp addedAt;
}
