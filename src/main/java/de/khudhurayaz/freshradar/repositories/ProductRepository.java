package de.khudhurayaz.freshradar.repositories;

import de.khudhurayaz.freshradar.model.Product;
import de.khudhurayaz.freshradar.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByUser_IdAndCategory_IdAndLocation_LocationId(Integer userId, Integer categoryId, Integer locationId);
    List<Product> findByUser_IdAndCategory_Id(Integer userId, Integer categoryId);
    List<Product> findByUser_IdAndLocation_LocationId(Integer userId, Integer locationId);
    List<Product> findByUser_Id(Integer userId);
    long countByUserId(int userId);
    List<Product> findAllByUser(User user);
}