package de.khudhurayaz.freshradar.dto;

import lombok.*;

import java.sql.Timestamp;

@Data
public class CategoryRequest {
    private Integer id;
    private String name;
}
