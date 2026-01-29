package com.example.hotels_app.repository;

import com.example.hotels_app.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    //for create
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.arrival < :departure " +
            "AND b.departure > :arrival")
    boolean existsOverlappingBooking(
            @Param("roomId") Long roomId,
            @Param("arrival") Instant arrival,
            @Param("departure") Instant departure
    );

    //for update
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.id <> :bookingId " +
            "AND b.arrival < :departure " +
            "AND b.departure > :arrival")
    boolean existsConflictingBooking(
            @Param("roomId") Long roomId,
            @Param("arrival") Instant arrival,
            @Param("departure") Instant departure,
            @Param("bookingId") Long bookingId);
}
