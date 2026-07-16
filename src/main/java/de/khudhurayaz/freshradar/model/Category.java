package de.khudhurayaz.freshradar.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "Category")
@AllArgsConstructor
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private int id;

    @OneToMany(mappedBy = "category")
    private List<Product> products;

    @Column(name = "category")
    private String category;

    @Column(name = "added_at")
    private Timestamp addedAt;
}
