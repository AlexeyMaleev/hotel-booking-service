package com.example.hotels_app.mapper;

import com.example.hotels_app.entity.Booking;
import com.example.hotels_app.model.request.CreateBookingRequest;
import com.example.hotels_app.model.request.UpdateBookingRequest;

import com.example.hotels_app.model.response.BookingResponse;
import org.mapstruct.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring",
        imports = {ZoneOffset.class, Instant.class})
public interface BookingMapper {

    @Mapping(target = "arrival", expression = "java(request.getArrival().atStartOfDay(ZoneOffset.UTC).toInstant())")
    @Mapping(target = "departure", expression = "java(request.getDeparture().atStartOfDay(ZoneOffset.UTC).toInstant())")
    Booking toEntity(CreateBookingRequest request);

    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.number", target = "roomNumber")
    @Mapping(source = "room.hotel.id", target = "hotelId")
    @Mapping(source = "room.hotel.name", target = "hotelName")
    BookingResponse toResponse(Booking booking);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "room", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntity(UpdateBookingRequest request,
                      @MappingTarget Booking booking);

    default Instant map(LocalDate date) {
        return date == null
                ? null
                : date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    default LocalDate map(Instant instant) {
        return instant == null ? null : instant.atZone(ZoneOffset.UTC).toLocalDate();
    }
}
