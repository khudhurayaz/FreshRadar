package de.khudhurayaz.freshradar.dto;

import de.khudhurayaz.freshradar.dto.inventory.InventoryRequest;
import lombok.*;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRequest {
    private List<ProfileRequest> profiles;
    private List<ProductRequest> products;
    private List<CategoryRequest> categories;
    private List<LocationRequest> locations;
    private List<InventoryRequest> inventories;
    private List<ContactRequest> contacts;
}
