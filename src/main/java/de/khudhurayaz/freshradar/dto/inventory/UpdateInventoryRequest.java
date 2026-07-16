package de.khudhurayaz.freshradar.dto.inventory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInventoryRequest {
    private Integer productId;
    private int quantity;
    private int currentQuantity;
}