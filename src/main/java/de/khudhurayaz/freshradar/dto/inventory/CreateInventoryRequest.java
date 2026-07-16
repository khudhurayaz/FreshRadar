package de.khudhurayaz.freshradar.dto.inventory;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInventoryRequest {
    private Integer productId;
    private int quantity;
    private int currentQuantity;
    private Timestamp added_at;
}