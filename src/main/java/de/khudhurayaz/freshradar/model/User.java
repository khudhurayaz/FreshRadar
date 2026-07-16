package de.khudhurayaz.freshradar.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.khudhurayaz.freshradar.model.setting.PagesSetting;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Column(name = "user_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Builder.Default
    @Column(name = "role")
    private String role = "user";

    @CreationTimestamp
    @Column(name = "account_created_at", updatable = false)
    private Timestamp accountCreatedAt;

    @Column(name = "last_login_at")
    private Timestamp lastLoginAt;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Subscription subscription;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Profile profile;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<Product> products;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PagesSetting adminSetting;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
