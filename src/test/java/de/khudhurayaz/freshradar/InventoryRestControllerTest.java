package de.khudhurayaz.freshradar;

import de.khudhurayaz.freshradar.controller.api.InventoryRestController;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.dto.inventory.CreateInventoryRequest;
import de.khudhurayaz.freshradar.dto.inventory.InventoryRequest;
import de.khudhurayaz.freshradar.dto.inventory.UpdateInventoryRequest;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.services.InventoryService;
import de.khudhurayaz.freshradar.services.ProductService;
import de.khudhurayaz.freshradar.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class InventoryRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private InventoryRestController inventoryRestController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(inventoryRestController)
                .build();
    }

    @Test
    void showInventories_returnsInventoryListForLoggedInUser() throws Exception {
        User user = new User();
        user.setId(1);

        InventoryRequest inventory = new InventoryRequest();
        inventory.setInventoryId(10);
        inventory.setProductId(5);
        inventory.setQuantity(20);
        inventory.setCurrentQuantity(15);
        inventory.setAdded_at(Timestamp.valueOf("2026-07-16 12:00:00"));

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(inventoryService.findAll(1))
                .thenReturn(List.of(inventory));

        mockMvc.perform(get("/api/showInventories")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].inventoryId").value(10))
                .andExpect(jsonPath("$[0].productId").value(5))
                .andExpect(jsonPath("$[0].quantity").value(20))
                .andExpect(jsonPath("$[0].currentQuantity").value(15));

        verify(userService).findByEmail("test@example.com");
        verify(inventoryService).findAll(1);
    }

    @Test
    void addInventory_returns200_whenInventoryIsCreated() throws Exception {
        CreateInventoryRequest createdInventory = new CreateInventoryRequest();
        createdInventory.setProductId(5);
        createdInventory.setQuantity(20);
        createdInventory.setCurrentQuantity(20);

        when(inventoryService.addInventory(any(CreateInventoryRequest.class)))
                .thenReturn(createdInventory);

        mockMvc.perform(put("/api/addInventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 5,
                                  "quantity": 20,
                                  "currentQuantity": 20
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventar wurde erfolgreich hinzugefügt."));

        verify(inventoryService).addInventory(any(CreateInventoryRequest.class));
    }

    @Test
    void addInventory_returns400_whenInventoryCannotBeCreated() throws Exception {
        when(inventoryService.addInventory(any(CreateInventoryRequest.class)))
                .thenReturn(null);

        mockMvc.perform(put("/api/addInventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "productId": 5,
                              "quantity": 20,
                              "currentQuantity": 20
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Es konnte kein neues Inventar hinzugefügt werden."));

        verify(inventoryService).addInventory(any(CreateInventoryRequest.class));
    }

    @Test
    void deleteInventory_returns200_whenInventoryWasDeleted() throws Exception {
        when(inventoryService.delete(10)).thenReturn(true);

        mockMvc.perform(delete("/api/deleteInventory/10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventory wurde erfolgreich gelöscht!"));

        verify(inventoryService).delete(10);
    }

    @Test
    void deleteInventory_returns404_whenInventoryCannotBeDeleted() throws Exception {
        when(inventoryService.delete(999)).thenReturn(false);

        mockMvc.perform(delete("/api/deleteInventory/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Inventory konnte nicht gelöscht werden!"));

        verify(inventoryService).delete(999);
    }

    @Test
    void updateInventory_returns200_whenInventoryWasUpdated() throws Exception {
        when(inventoryService.save(any(UpdateInventoryRequest.class)))
                .thenReturn(true);

        mockMvc.perform(put("/api/updateInventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 5,
                                  "quantity": 20,
                                  "currentQuantity": 12
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Inventar wurde erfolgreich aktualisiert!"));

        verify(inventoryService).save(any(UpdateInventoryRequest.class));
    }

    @Test
    void updateInventory_returns404_whenInventoryCannotBeUpdated() throws Exception {
        when(inventoryService.save(any(UpdateInventoryRequest.class)))
                .thenReturn(false);

        mockMvc.perform(put("/api/updateInventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 5,
                                  "quantity": 20,
                                  "currentQuantity": 12
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Inventar konnte nicht aktualisiert werden!"));

        verify(inventoryService).save(any(UpdateInventoryRequest.class));
    }

    @Test
    void getInventory_returns200_whenProductAndInventoryExist() throws Exception {
        User user = new User();
        user.setId(1);

        ProductRequest product = new ProductRequest();
        product.setId(5);

        InventoryRequest inventory = new InventoryRequest();
        inventory.setInventoryId(10);
        inventory.setProductId(5);
        inventory.setQuantity(20);
        inventory.setCurrentQuantity(12);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(productService.findByProductId(5))
                .thenReturn(Optional.of(product));
        when(inventoryService.findByProductId(5))
                .thenReturn(Optional.of(inventory));

        mockMvc.perform(get("/api/inventory/5")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryId").value(10))
                .andExpect(jsonPath("$.productId").value(5))
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.currentQuantity").value(12));

        verify(userService).findByEmail("test@example.com");
        verify(productService).findByProductId(5);
        verify(inventoryService).findByProductId(5);
    }

    @Test
    void getInventory_returns404_whenProductDoesNotExist() throws Exception {
        User user = new User();
        user.setId(1);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(productService.findByProductId(999))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/999")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isNotFound());

        verify(userService).findByEmail("test@example.com");
        verify(productService).findByProductId(999);
    }

    @Test
    void healthCareCheckup_returns401_whenUserDoesNotExist() throws Exception {
        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/inventory/healthCareCheckup")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("[Inventar] Benutzer nicht gefunden!"));

        verify(userService).findByEmail("test@example.com");
    }

    @Test
    void healthCareCheckup_returns404_whenInventoryIsEmpty() throws Exception {
        User user = new User();
        user.setId(1);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(inventoryService.findAll(1))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/inventory/healthCareCheckup")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("[Inventar] Keine Einträge wurden gefunden!"));

        verify(userService).findByEmail("test@example.com");
        verify(inventoryService).findAll(1);
    }

    @Test
    void healthCareCheckup_returns404_whenInventoryIsUnderSupplied() throws Exception {
        User user = new User();
        user.setId(1);

        InventoryRequest inventory = new InventoryRequest();
        inventory.setInventoryId(10);
        inventory.setProductId(5);
        inventory.setQuantity(20);
        inventory.setCurrentQuantity(3);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(inventoryService.findAll(1))
                .thenReturn(List.of(inventory));

        mockMvc.perform(get("/api/inventory/healthCareCheckup")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("[Inventar] Unterversorgt! Bitte nachkaufen!"));

        verify(userService).findByEmail("test@example.com");
        verify(inventoryService).findAll(1);
    }

    @Test
    void healthCareCheckup_returns200_whenInventoryIsSufficient() throws Exception {
        User user = new User();
        user.setId(1);

        InventoryRequest inventory = new InventoryRequest();
        inventory.setInventoryId(10);
        inventory.setProductId(5);
        inventory.setQuantity(20);
        inventory.setCurrentQuantity(10);

        when(userService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(inventoryService.findAll(1))
                .thenReturn(List.of(inventory));

        mockMvc.perform(get("/api/inventory/healthCareCheckup")
                        .principal(() -> "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("[Inventar] Bestens versorgt! Der Vorrat ist ausreichend."));

        verify(userService).findByEmail("test@example.com");
        verify(inventoryService).findAll(1);
    }
}