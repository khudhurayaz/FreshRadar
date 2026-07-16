package de.khudhurayaz.freshradar.repositories;

import de.khudhurayaz.freshradar.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategory(String name);
    List<Category> findAll();

}
