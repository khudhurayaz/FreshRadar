package de.khudhurayaz.freshradar.dto.inventory;

import lombok.*;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryRequest {

    private Integer inventoryId;
    private Integer productId;
    private int quantity;
    private int currentQuantity;
    private Timestamp added_at;

}
