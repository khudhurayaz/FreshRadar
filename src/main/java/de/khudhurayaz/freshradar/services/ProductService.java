package de.khudhurayaz.freshradar.services;

import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.dto.inventory.CreateInventoryRequest;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.model.Category;
import de.khudhurayaz.freshradar.model.Inventory;
import de.khudhurayaz.freshradar.model.Product;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.repositories.ProductRepository;
import de.khudhurayaz.freshradar.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j2
@Service
@AllArgsConstructor
public class ProductService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final LocationService locationService;
    private final CategoryService categoryService;

    public Optional<ProductRequest> getProduct(Integer id) {
        return productRepository.findById(id).map(this::setProductRequest);
    }

    @Transactional
    public boolean createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setUnit(request.getUnit());
        product.setIsOpen(request.getIsOpen());

        if (request.getUserId() != null) {
            userRepository.findById(request.getUserId())
                    .ifPresent(product::setUser);
        } else {
            throw new IllegalArgumentException("Produkt kann nicht ohne Benutzer erstellt werden.");
        }

        if (request.getAddedAt() != null) {
            product.setAddedAt(request.getAddedAt());
        } else {
            product.setAddedAt(new Timestamp(System.currentTimeMillis()));
        }

        product.setExpiryDate(request.getExpiryDate());

        if (request.getLocationId() != null && request.getLocationId() != 0) {
            locationService.findById(request.getLocationId())
                    .ifPresent(product::setLocation);
        }

        if (request.getCategoryId() != null && request.getCategoryId() != 0) {
            Optional<CategoryRequest> categoryRequest = categoryService.findById(request.getCategoryId());
            categoryRequest.ifPresent(v -> {
                Category category = new Category();
                category.setId(v.getId());
                category.setCategory(v.getName());
                product.setCategory(category);
            });
        }

        Product savedProduct = productRepository.save(product);

        CreateInventoryRequest inventoryRequest = new CreateInventoryRequest();
        inventoryRequest.setProductId(savedProduct.getProductId());
        inventoryRequest.setQuantity(request.getSoll() != null ? request.getSoll() : 0);
        inventoryRequest.setCurrentQuantity(request.getIst() != null ? request.getIst() : 0);
        inventoryRequest.setAdded_at(savedProduct.getAddedAt());
        inventoryService.save(inventoryRequest, 0);

        return true;
    }

    @Transactional
    public boolean update(int productId, ProductRequest request) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return false;

        if (request.getLocationId() != null) {
            locationService.findById(request.getLocationId())
                    .ifPresent(product::setLocation);
        }

        if (request.getUserId() != null) {
            Optional<User> user = userRepository.findById(request.getUserId());
            user.ifPresent(product::setUser);
        }

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getUnit() != null) {
            product.setUnit(request.getUnit());
        }
        if (request.getIsOpen() != null) {
            product.setIsOpen(request.getIsOpen());
        }
        if (request.getExpiryDate() != null) {
            product.setExpiryDate(request.getExpiryDate());
        }

        List<Inventory> inventories = inventoryService.findByProduct(product);
        Inventory inventory = inventories.stream().findFirst().orElse(null);

        if (inventory == null) {
            if (request.getSoll() != null || request.getIst() != null) {
                CreateInventoryRequest createRequest = new CreateInventoryRequest();
                createRequest.setProductId(product.getProductId());
                createRequest.setQuantity(request.getSoll() != null ? request.getSoll() : 0);
                createRequest.setCurrentQuantity(request.getIst() != null ? request.getIst() : 0);
                createRequest.setAdded_at(new Timestamp(System.currentTimeMillis()));
                inventoryService.save(createRequest, 0);
            }
        } else {
            CreateInventoryRequest updateRequest = new CreateInventoryRequest();
            updateRequest.setProductId(product.getProductId());
            updateRequest.setQuantity(request.getSoll() != null ? request.getSoll() : inventory.getShould());
            updateRequest.setCurrentQuantity(request.getIst() != null ? request.getIst() : inventory.getCurrentQuantity());
            updateRequest.setAdded_at(inventory.getAddedAt());
            inventoryService.save(updateRequest, inventory.getInventoryId());
        }

        productRepository.save(product);
        return true;
    }

    public List<ProductRequest> getFilteredProductsAsDto(Integer kategorieId, Integer lagerort, Integer userId) {
        List<Product> products = getFilteredProducts(kategorieId, lagerort, userId);
        return products.stream().map(this::mapToProductRequestWithInventory).toList();
    }

    public List<ProductRequest> expiringProducts(int userId) {
        LocalDate today = LocalDate.now();

        return productRepository.findAll().stream()
                .filter(product -> product.getUser() != null && product.getUser().getId() == userId)
                .filter(product -> product.getExpiryDate() != null)
                .filter(product -> product.getExpiryDate()
                        .toLocalDateTime()
                        .toLocalDate()
                        .isBefore(today))
                .map(this::setProductRequest)
                .toList();
    }

    public List<ProductRequest> getExpiringProducts(int userId) {
        LocalDate today = LocalDate.now();

        return productRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId() == userId)
                .map(this::setProductRequest)
                .filter(req -> {
                    if (req.getExpiryDate() == null) return false;

                    LocalDate expiryDate = req.getExpiryDate().toLocalDateTime().toLocalDate();
                    if (expiryDate.isBefore(today)) return false;

                    long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, expiryDate);
                    long maxDays = Boolean.TRUE.equals(req.getIsOpen()) ? 2 : 4;

                    return daysRemaining >= 0 && daysRemaining <= maxDays;
                })
                .toList();
    }

    public List<CategoryRequest> getAllCategories() {
        return categoryService.findAll();
    }

    public long countProductsByUser(int userId) {
        return productRepository.countByUserId(userId);
    }

    public List<ProductRequest> getAll(int userId) {
        return productRepository.findAll().stream()
                .filter(p -> p.getUser() != null && p.getUser().getId() == userId)
                .map(product -> {
                    ProductRequest dto = setProductRequest(product);
                    if (product.getInventories() != null && !product.getInventories().isEmpty()) {
                        dto.setSoll(product.getInventories().stream().mapToInt(Inventory::getShould).sum());
                        dto.setIst(product.getInventories().stream().mapToInt(Inventory::getCurrentQuantity).sum());
                    } else {
                        dto.setSoll(0);
                        dto.setIst(0);
                    }
                    ProductRequest pr = mapToProductRequestWithInventory(product);
                    dto.setVorratProzent(pr.getVorratProzent());
                    dto.setVorratFarbe(pr.getVorratFarbe());
                    return dto;
                })
                .toList();
    }

    public List<ProductRequest> findByUser(User user) {
        return productRepository.findAllByUser(user)
                .stream()
                .map(product -> {
                    ProductRequest request = new ProductRequest();
                    request.setId(product.getProductId());
                    request.setName(product.getName());
                    return request;
                })
                .toList();
    }

    public Optional<List<ProductRequest>> findByUserId(int userId) {
        return Optional.of(productRepository.findByUser_Id(userId)
                .stream()
                .filter(p -> p.getUser() != null && p.getUser().getId() == userId)
                .map(this::setProductRequest)
                .toList());
    }

    public Optional<ProductRequest> findByProductId(int id) {
        return productRepository.findById(id).map(this::setProductRequest);
    }

    public Optional<ProductRequest> delete(int id) {
        Optional<Product> p = productRepository.findById(id);
        if (p.isPresent()) {
            productRepository.deleteById(id);
            return p.map(this::setProductRequest);
        } else {
            return Optional.empty();
        }
    }

    public Optional<List<ProductRequest>> findAll() {
        return Optional.of(productRepository.findAll().stream()
                .map(product -> {
                    ProductRequest dto = setProductRequest(product);
                    if (product.getInventories() != null && !product.getInventories().isEmpty()) {
                        dto.setSoll(product.getInventories().stream().mapToInt(Inventory::getShould).sum());
                        dto.setIst(product.getInventories().stream().mapToInt(Inventory::getCurrentQuantity).sum());
                    } else {
                        dto.setSoll(0);
                        dto.setIst(0);
                    }
                    return dto;
                })
                .toList());
    }

    private List<Product> getFilteredProducts(Integer categoryId, Integer lagerId, Integer userId) {
        log.info("Suche Produkte für User-ID={} mit Kategorie-ID={} und Lager-ID={}", userId, categoryId, lagerId);

        if (categoryId != null && lagerId != null) {
            return productRepository.findByUser_IdAndCategory_IdAndLocation_LocationId(userId, categoryId, lagerId);
        } else if (categoryId != null) {
            return productRepository.findByUser_IdAndCategory_Id(userId, categoryId);
        } else if (lagerId != null) {
            return productRepository.findByUser_IdAndLocation_LocationId(userId, lagerId);
        }

        return productRepository.findByUser_Id(userId);
    }

    private ProductRequest mapToProductRequestWithInventory(Product p) {
        ProductRequest dto = setProductRequest(p);
        List<Inventory> inventories = inventoryService.findByProduct(p);

        int prozent = 0;
        String farbe = "bg-danger";

        if (!inventories.isEmpty()) {
            int soll = inventories.stream().mapToInt(Inventory::getShould).sum();
            int ist = inventories.stream().mapToInt(Inventory::getCurrentQuantity).sum();

            if (soll > 0) {
                prozent = (ist * 100) / soll;
                if (prozent >= 70) farbe = "bg-success";
                else if (prozent >= 40) farbe = "bg-warning";
            }
        }

        for (Inventory inventory : inventories) {
            if (inventory != null) {
                if (Objects.equals(inventory.getProduct().getProductId(), dto.getId())) {
                    dto.setSoll(inventory.getShould());
                    dto.setIst(inventory.getCurrentQuantity());
                }
            }
        }

        dto.setVorratProzent(prozent);
        dto.setVorratFarbe(farbe);
        return dto;
    }

    private ProductRequest setProductRequest(Product p) {
        ProductRequest dto = new ProductRequest();
        dto.setId(p.getProductId());
        dto.setUserId(p.getUser().getId());
        dto.setCategoryId(p.getCategory().getId());
        dto.setLocationId(p.getLocation() != null ? p.getLocation().getLocationId() : null);
        dto.setName(p.getName());
        dto.setIsOpen(p.getIsOpen());
        dto.setUnit(p.getUnit());
        dto.setExpiryDate(p.getExpiryDate());
        dto.setAddedAt(p.getAddedAt());
        return dto;
    }
}