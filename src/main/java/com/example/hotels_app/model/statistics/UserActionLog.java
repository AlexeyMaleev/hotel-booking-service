package com.example.hotels_app.model.statistics;

import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_statistics")
public class UserActionLog {

    @Id
    private String id;

    private Long userId;

    private String actionType;

    private LocalDate arrival;

    private LocalDate departure;

    private Instant createdAt = Instant.now();
}
