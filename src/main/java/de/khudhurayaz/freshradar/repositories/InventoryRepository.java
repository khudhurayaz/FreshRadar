package de.khudhurayaz.freshradar.repositories;

import de.khudhurayaz.freshradar.model.Inventory;
import de.khudhurayaz.freshradar.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    List<Inventory> findByProduct(Product product);
    Optional<Inventory> findByProduct_ProductId(int productId);
}