package com.example.hotels_app.statistics.service;

import com.example.hotels_app.model.statistics.UserActionLog;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class CsvExportService {

    public Flux<String> getStatisticCsv(Flux<UserActionLog> logs){

        String header = "ID,ActionType,UserID,Arrival,Departure,CreatedAt\n";

        return Flux.concat(
                Flux.just(header),
                logs.map(log -> String.format("%s,%s,%s,%s,%s,%s\n",
                        log.getId(),
                        log.getActionType(),
                        log.getUserId(),
                        log.getArrival() != null ? log.getArrival() : "",
                        log.getDeparture() != null ? log.getDeparture() : "",
                        log.getCreatedAt()
                ))
        );
    }
}
