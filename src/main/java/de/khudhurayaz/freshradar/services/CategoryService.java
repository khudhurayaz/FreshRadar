package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.model.Category;
import de.khudhurayaz.freshradar.repositories.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Log4j2
public class CategoryService {
    private final CategoryRepository repository;

    public Optional<Boolean> add(CategoryRequest request) {
        Category category = new Category();
        category.setAddedAt(new Timestamp(System.currentTimeMillis()));
        category.setCategory(request.getName());
        Category savedCategory = repository.save(category);
        return Optional.of(savedCategory != null);
    }
    public Optional<CategoryRequest> findByCategory(String name) {
        // 1. Suche die Kategorie
        Optional<Category> categoryOpt = repository.findByCategory(name);

        // 2. Sicher prüfen, ob sie gefunden wurde
        if (categoryOpt.isEmpty()) {
            return Optional.empty(); // Gibt ein leeres Optional zurück, statt abzustürzen
        }

        // 3. Mapping durchführen
        Category cat = categoryOpt.get();
        CategoryRequest request = new CategoryRequest();
        request.setId(cat.getId());
        request.setName(cat.getCategory());

        return Optional.of(request);
    }

    public Optional<CategoryRequest> findById(Integer id) {
        return repository.findById(id) // Nutzt die Standard-JPA Methode
                .map(cat -> {
                    CategoryRequest request = new CategoryRequest();
                    request.setId(cat.getId());
                    request.setName(cat.getCategory());
                    return request;
                });
    }

    public List<CategoryRequest> findAll(){
        return repository.findAll().stream()
                .map(c -> {
                    CategoryRequest cr = new CategoryRequest();
                    cr.setId(c.getId());
                    cr.setName(c.getCategory());
                    return cr;
                })
                .toList();
    }
}
