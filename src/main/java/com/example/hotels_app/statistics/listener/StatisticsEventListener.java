package com.example.hotels_app.statistics.listener;

import com.example.hotels_app.model.event.BookingEvent;
import com.example.hotels_app.model.event.UserRegistrationEvent;
import com.example.hotels_app.model.statistics.ActionType;
import com.example.hotels_app.model.statistics.UserActionLog;
import com.example.hotels_app.statistics.repository.HotelStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@Component
@Slf4j
@RequiredArgsConstructor
public class StatisticsEventListener {

    private final HotelStatisticsRepository statisticsRepository;

    private final ReceiverOptions<String, Object> receiverOptions;

    @EventListener(ApplicationReadyEvent.class)
    public void setup(){

        KafkaReceiver.create(receiverOptions)
                .receive()
                .flatMap( record -> {
                    Object event = record.value();
                    log.info("Received event from Kafka: {}", event);

                    UserActionLog logEntry = mapToLog(event);

                    return  statisticsRepository.save(logEntry)
                            .doOnNext(saved -> log.info("=== MONGO NEXT: {}", saved))
                            .doOnSuccess(saved -> {
                                log.info("=== MONGO SUCCESS: {}", saved.getId());
                                record.receiverOffset().acknowledge();
                            })
                            .doOnError(e -> log.error("Error saving to MongoDB", e));
                })
                .subscribe();
    }

    private UserActionLog mapToLog(Object event) {

        UserActionLog logEntry = new UserActionLog();

        if(event instanceof UserRegistrationEvent registrationEvent){
            logEntry.setUserId(registrationEvent.getUserId());
            //logEntry.setActionType("REGISTRATION");
            logEntry.setActionType(ActionType.REGISTRATION.name());
        } else if(event instanceof BookingEvent bookingEvent){
            logEntry.setUserId(bookingEvent.getUserId());
            //logEntry.setActionType("BOOKING");
            logEntry.setActionType(ActionType.BOOKING.name());
            logEntry.setArrival(bookingEvent.getArrival());
            logEntry.setDeparture(bookingEvent.getDeparture());
        }

        return logEntry;
    }
}
