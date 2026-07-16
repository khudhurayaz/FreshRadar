package de.khudhurayaz.freshradar.controller.api;

import de.khudhurayaz.freshradar.dto.CategoryRequest;
import de.khudhurayaz.freshradar.dto.ProductRequest;
import de.khudhurayaz.freshradar.model.Location;
import de.khudhurayaz.freshradar.model.User;
import de.khudhurayaz.freshradar.services.CategoryService;
import de.khudhurayaz.freshradar.services.LocationService;
import de.khudhurayaz.freshradar.services.ProductService;
import de.khudhurayaz.freshradar.services.UserService;
import de.khudhurayaz.freshradar.util.ShelfLifeCalculator;
import de.khudhurayaz.freshradar.util.Util;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product/")
@Log4j2
public class ProductRestController {

    private final ProductService productService;
    public final static int LIMIT = 10;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LocationService locationService;

    public ProductRestController(ProductService productService, UserService userService, CategoryService categoryService, LocationService locationService) {
        this.productService = productService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.locationService = locationService;
    }

    @GetMapping("/showProductsWithCategoryAndLocation")
    public ResponseEntity<List<ProductRequest>> getProdukte(
            Principal principal,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer location) {

        return userService.findByEmail(principal.getName())
                .map(user -> {
                    List<ProductRequest> dtos = productService.getFilteredProductsAsDto(category, location, user.getId());
                    return ResponseEntity.ok(dtos);
                })
                .orElse(ResponseEntity.status(401).build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductRequest>> getAllProducts(Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());
        List<ProductRequest> dtos = productService.getAll(user.get().getId());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateProduct(
            @PathVariable int id,
            @RequestBody ProductRequest updatedData
    ) {
        Optional<CategoryRequest> categoryOpt = categoryService.findById(updatedData.getCategoryId());
        Optional<Location> locationOpt = locationService.findById(updatedData.getLocationId());

        if (categoryOpt.isEmpty() || locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Kategorie oder Lagerort ungültig.");
        }

        String catName = categoryOpt.get().getName().toUpperCase();
        String locName = locationOpt.get().getLocation().toUpperCase();
        Timestamp newExpiry = updatedData.getExpiryDate();
        LocalDate isOpenDate = null;
        LocalDate today = LocalDate.now();

        ShelfLifeCalculator.ProductCategory cat = null;
        try {
            cat = ShelfLifeCalculator.ProductCategory.valueOf(catName);
        } catch (IllegalArgumentException e) {
            if (!updatedData.getIsOpen()) {
                log.warn("Kategorie {} ist nicht im Enum definiert, verwende Original-MHD.", catName);
            } else {
                isOpenDate = today.plusDays(4);
            }
        }
        log.debug("Gib Ort aus {}", locName);
        if (cat != null) {
            log.debug("ich bin in if abfrage");
            boolean isSpecialStorage = locName.equals("GEFRIERFACH") || cat.isStorageForbidden(locName);
            log.debug("isSpecialStorage {}", isSpecialStorage);
            if (Boolean.TRUE.equals(updatedData.getIsOpen()) || isSpecialStorage) {
                if (newExpiry == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MHD fehlt.");

                try {
                    newExpiry = ShelfLifeCalculator.calculateAsTimestamp(cat, locName, newExpiry);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
                }
            }
        }
        if (isOpenDate != null) {
            updatedData.setExpiryDate(Timestamp.valueOf(isOpenDate.atStartOfDay()));
        } else {
            updatedData.setExpiryDate(newExpiry);
        }
        boolean saved = productService.update(id, updatedData);
        return saved ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable int id
    ) {
        Optional<ProductRequest> r = productService.delete(id);
        if (r.isPresent()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createProduct(
            Principal principal,
            @RequestBody ProductRequest createProduct
    ) {
        Optional<User> user = userService.findByEmail(principal.getName());
        Optional<List<ProductRequest>> productRequests = productService.findByUserId(user.get().getId());

        // Limit-Check
        if (productRequests.get().size() >= LIMIT && "basic".equalsIgnoreCase(user.get().getSubscription().getPlanType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Du hast das Limit von " + LIMIT + " Produkten im Basic-Plan erreicht. <br>Upgrade auf Pro-Plan, um mehr produkte hinzuzufügen!");
        }

        // Kategorie check
        if (createProduct.getCategoryId() == null || createProduct.getCategoryId() == 0) {
            return new ResponseEntity<>("Du hast keine Kategorie ausgewählt!", HttpStatus.BAD_REQUEST);
        }

        user.ifPresent(value -> createProduct.setUserId(value.getId()));

        // Daten abrufen
        Optional<CategoryRequest> categoryOpt = categoryService.findById(createProduct.getCategoryId());
        Optional<Location> locationOpt = locationService.findById(createProduct.getLocationId());

        if (categoryOpt.isEmpty() || locationOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Kategorie oder Lagerort ungültig.");
        }

        String catName = categoryOpt.get().getName().toUpperCase();
        String locName = locationOpt.get().getLocation().toUpperCase();
        Timestamp newExpiry = createProduct.getExpiryDate();

        ShelfLifeCalculator.ProductCategory cat = null;
        try {
            cat = ShelfLifeCalculator.ProductCategory.valueOf(catName);
        } catch (IllegalArgumentException e) {
           log.warn("Kategorie {} ist nicht im Enum definiert, verwende Original-MHD.", catName);
        }

        if (cat != null) {
            try {
                boolean isSpecialStorage = locName.equals("GEFRIERFACH") || cat.isStorageForbidden(locName);
                log.debug("isSpecialStorage: {}", isSpecialStorage);
                if (createProduct.getIsOpen() || isSpecialStorage) {
                    if (newExpiry == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("MHD fehlt.");
                    }

                    newExpiry = ShelfLifeCalculator.calculateAsTimestamp(cat, locName, newExpiry);
                    log.debug("newExpiry wurde gesetzt und lautet: {}", newExpiry);
                }
            } catch (IllegalArgumentException e) {
                log.debug("Validierungsfehler: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }

        createProduct.setExpiryDate(newExpiry);
        log.debug("Product: {}", createProduct);

        boolean created = productService.createProduct(createProduct);

        return created
                ? ResponseEntity.status(HttpStatus.CREATED).body("Das Produkt wurde erfolgreich hinzugefügt!")
                : ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ein Fehler ist aufgetreten!");
    }

    @GetMapping("/expiring")
    public ResponseEntity<Long> getExpiringCount( Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());
        List<ProductRequest> expiring = productService.getExpiringProducts(user.get().getId());
        return ResponseEntity.ok((long) expiring.size());
    }

    @GetMapping("/expiring/expiringProducts")
    public ResponseEntity<List<ProductRequest>> getExpiringStandardfall(Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());
        List<ProductRequest> expiring = productService.expiringProducts(user.get().getId());
        return ResponseEntity.ok().body(expiring);
    }

    @GetMapping("/limit")
    public boolean limit(Principal principal) {
        Optional<User> user = userService.findByEmail(principal.getName());
        Optional<List<ProductRequest>> productRequests = productService.findByUserId(user.get().getId());
        return productRequests.get().size() == LIMIT;
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductRequest>> getProductsPaged(
            Principal principal,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer location,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {

        Optional<User> userOptional = userService.findByEmail(principal.getName());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        int userId = userOptional.get().getId();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 5), 50);

        List<ProductRequest> products;

        if (category != null || location != null) {
            products = productService.getFilteredProductsAsDto(category, location, userId);
        } else {
            products = productService.getAll(userId);
        }

        Page<ProductRequest> productPage = Util.getPages(safePage, safeSize, products);

        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductRequest> getProduct(@PathVariable Integer id) {
        Optional<ProductRequest> request = productService.getProduct(id);
        return request.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }
}