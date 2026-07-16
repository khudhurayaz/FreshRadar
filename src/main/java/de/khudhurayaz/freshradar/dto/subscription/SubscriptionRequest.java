package de.khudhurayaz.freshradar.dto.subscription;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {
    private int id;
    private int userId;
    private String planType;
    private String status;
    private Timestamp purchasedAt;
}
