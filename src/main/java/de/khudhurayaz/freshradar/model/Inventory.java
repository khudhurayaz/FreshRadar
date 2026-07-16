package de.khudhurayaz.freshradar.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "inventory")
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "should")
    private int should;

    @Column(name = "current_quantity")
    private int currentQuantity;

    @Column(name = "added_at")
    private Timestamp addedAt;
}
