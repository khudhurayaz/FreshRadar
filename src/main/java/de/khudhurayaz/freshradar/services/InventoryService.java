package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.inventory.CreateInventoryRequest;
import de.khudhurayaz.freshradar.dto.inventory.InventoryRequest;
import de.khudhurayaz.freshradar.dto.inventory.UpdateInventoryRequest;
import de.khudhurayaz.freshradar.model.Inventory;
import de.khudhurayaz.freshradar.model.Product;
import de.khudhurayaz.freshradar.repositories.InventoryRepository;
import de.khudhurayaz.freshradar.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log4j2
@ToString
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public List<Inventory> findByProduct(Product product) {
        return inventoryRepository.findByProduct(product);
    }

    public Optional<InventoryRequest> findByProductId(int productId) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isPresent()) {
            Optional<Inventory> inventory = inventoryRepository.findByProduct_ProductId(product.get().getProductId());
            if (inventory.isPresent()) {
                return Optional.of(setInventoryRequest(inventory.get()));
            }
        }
        return Optional.empty();
    }

    public CreateInventoryRequest addInventory(CreateInventoryRequest inventoryRequest) {
        Optional<Product> product = productRepository.findById(inventoryRequest.getProductId());
        if (product.isEmpty()) {
            log.error("Das produkt mit der productId={} wurde nicht gefunden!", inventoryRequest.getProductId());
            return null;
        }

        Inventory inventory = new Inventory();
        inventory.setProduct(product.get());
        inventory.setShould(inventoryRequest.getQuantity());
        inventory.setCurrentQuantity(inventoryRequest.getCurrentQuantity());
        inventory.setAddedAt(inventoryRequest.getAdded_at());
        return setCreateInventoryRequest(inventoryRepository.save(inventory));
    }

    /**
     * Es werden alles, was in Inventar ist ausgegeben!
     * Bedingung ist das, sich ein Benutzer registiert hat und
     * dementsprechend die von User hinzugefügten Items angezeigt!
     * @param userId Erwartet das UserId, ohne UserId wird es nicht gefiltet bzw. ein leerer Array zurückgegeben.
     * @return Eine Liste, die es schon gefiltert ist nach User.
     */
    public List<InventoryRequest> findAll(int userId) {
        return inventoryRepository.findAll().stream()
                .filter(inventory -> inventory.getProduct().getUser() != null && inventory.getProduct().getUser().getId() == userId)
                .map(inventory -> {
                    InventoryRequest inventoryRequest = new InventoryRequest();
                    inventoryRequest.setInventoryId(inventory.getInventoryId());
                    inventoryRequest.setProductId(inventory.getProduct().getProductId());
                    inventoryRequest.setQuantity(inventory.getShould());
                    inventoryRequest.setCurrentQuantity(inventory.getCurrentQuantity());
                    inventoryRequest.setAdded_at(inventory.getAddedAt());
                    return inventoryRequest;
                }).toList();
    }

    public boolean delete(int id) {
        return inventoryRepository.findById(id)
                .map(inventory -> {
                    inventoryRepository.delete(inventory);
                    return true;
                })
                .orElse(false);
    }

    public boolean deleteByProductId(int productId) {
        return inventoryRepository.findByProduct_ProductId(productId)
                .map(inventory -> {
                    inventoryRepository.delete(inventory);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean save(UpdateInventoryRequest request) {
        // 1. Suche das existierende Inventory
        Optional<Inventory> inventoryOpt = inventoryRepository.findByProduct_ProductId(request.getProductId());

        if (inventoryOpt.isPresent()) {

            Inventory inventory = inventoryOpt.get();

            // 2. Werte direkt auf das Entity-Objekt übertragen (Mapping)
            inventory.setShould(request.getQuantity());
            inventory.setCurrentQuantity(request.getCurrentQuantity());
            return true;
        }

        return false; // Produkt nicht gefunden
    }

    public boolean save(CreateInventoryRequest request, int id) {
        if (id == 0) {
            Inventory newInventory = new Inventory();

            productRepository.findById(request.getProductId()).ifPresent(product -> {
                newInventory.setProduct(product);
            });

            newInventory.setShould(request.getQuantity());
            newInventory.setCurrentQuantity(request.getCurrentQuantity());
            newInventory.setAddedAt(request.getAdded_at());

            inventoryRepository.save(newInventory);
            return true;
        }

        return inventoryRepository.findById(id)
                .filter(prod -> prod.getProduct().getProductId().equals(request.getProductId()))
                .map(inventory -> {
                    inventory.setShould(request.getQuantity());
                    inventory.setCurrentQuantity(request.getCurrentQuantity());
                    inventory.setAddedAt(request.getAdded_at());
                    inventoryRepository.save(inventory);
                    return true;
                })
                .orElse(false);
    }

    private CreateInventoryRequest setCreateInventoryRequest(Inventory inventory) {
        return new CreateInventoryRequest(
                inventory.getProduct().getProductId(),
                inventory.getShould(), inventory.getCurrentQuantity(),
                inventory.getAddedAt());
    }

    private InventoryRequest setInventoryRequest(Inventory inventory) {
        return new InventoryRequest(
                inventory.getInventoryId(),
                inventory.getProduct().getProductId(),
                inventory.getShould(), inventory.getCurrentQuantity(),
                inventory.getAddedAt()
        );
    }

}
