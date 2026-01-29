package com.example.hotels_app.repository;

import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> , JpaSpecificationExecutor<Room> {

    boolean existsByNumberAndHotelId(Integer number, Long hotelId);

    boolean existsByNumberAndHotelIdAndIdNot(Integer number, Long hotelId, Long roomId);
}
