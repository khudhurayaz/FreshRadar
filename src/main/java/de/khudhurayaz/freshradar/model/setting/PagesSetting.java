package de.khudhurayaz.freshradar.model.setting;

import de.khudhurayaz.freshradar.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "pages_settings")
@AllArgsConstructor
@NoArgsConstructor
public class PagesSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "profile_page_size", nullable = false)
    private int profilePageSize = 10;

    @Column(name = "product_page_size", nullable = false)
    private int productPageSize = 10;

    @Column(name = "contact_page_size", nullable = false)
    private int contactPageSize = 10;

    @Column(name = "user_page_size", nullable = false)
    private int userPageSize = 10;
}