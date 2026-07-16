package de.khudhurayaz.freshradar.dto.subscription;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReactiveRequest {
    private int userId;
    private int subscriptionId;
    private String status;
}
