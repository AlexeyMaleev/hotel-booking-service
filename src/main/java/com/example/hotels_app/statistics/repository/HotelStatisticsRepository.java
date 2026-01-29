package com.example.hotels_app.statistics.repository;

import com.example.hotels_app.model.statistics.UserActionLog;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface HotelStatisticsRepository extends ReactiveMongoRepository<UserActionLog,String> {
}
