package de.khudhurayaz.freshradar.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Builder
@Table(name = "subscription")
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    @Column(name = "subscription_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(name = "plan_type")
    private String planType;

    @Column(name = "status")
    private String status;

    @Column(name = "purchased_at")
    private Timestamp purchasedAt;
}
