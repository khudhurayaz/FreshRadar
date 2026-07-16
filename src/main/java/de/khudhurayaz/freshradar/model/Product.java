package de.khudhurayaz.freshradar.model;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "product")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    // Beziehung zum User: Viele Produkte gehören zu einem User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inventory> inventories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category  category;

    @Column(name = "name")
    private String name;

    @Column(name = "is_open")
    private Boolean isOpen;

    @Column(name = "unit")
    private String unit;

    @Column(name = "expiry_date")
    private Timestamp expiryDate;

    @Column(name = "added_at")
    private Timestamp addedAt;

}