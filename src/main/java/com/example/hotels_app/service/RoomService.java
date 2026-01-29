package com.example.hotels_app.service;

import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.exception.HotelNotFoundException;
import com.example.hotels_app.exception.RoomAlreadyExistsException;
import com.example.hotels_app.exception.RoomNotFoundException;
import com.example.hotels_app.mapper.RoomMapper;
import com.example.hotels_app.model.request.*;
import com.example.hotels_app.model.response.HotelListResponse;
import com.example.hotels_app.model.response.RoomListResponse;
import com.example.hotels_app.model.response.RoomResponse;
import com.example.hotels_app.repository.HotelRepository;
import com.example.hotels_app.repository.RoomRepository;
import com.example.hotels_app.repository.specification.HotelSpecifications;
import com.example.hotels_app.repository.specification.RoomSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final HotelRepository hotelRepository;


    private  final RoomMapper roomMapper;

    public RoomResponse findById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(()-> new RoomNotFoundException(id));
        return roomMapper.toResponse(room);
    }

    public RoomResponse create(CreateRoomRequest request) {

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));

        if (roomRepository.existsByNumberAndHotelId(request.getNumber(), request.getHotelId())) {
            throw new RoomAlreadyExistsException(request.getHotelId(), request.getNumber());
        }

        Room room = roomMapper.toEntity(request);
        room.setHotel(hotel);

        return roomMapper.toResponse(roomRepository.save(room));
    }

    public RoomResponse update(Long id, UpdateRoomRequest request) {

        Room room = roomRepository.findById(id)
                .orElseThrow(()-> new RoomNotFoundException(id));

        // Определяем финальные значения (текущие или новые из запроса)
        Integer targetNumber = (request.getNumber() != null) ? request.getNumber() : room.getNumber();
        Long targetHotelId = (request.getHotelId() != null) ? request.getHotelId() : room.getHotel().getId();

        // Проверяем уникальность номера в рамках отеля
        boolean alreadyExists = roomRepository.existsByNumberAndHotelIdAndIdNot(targetNumber, targetHotelId, id);

        if (alreadyExists) {
            throw new RoomAlreadyExistsException(targetHotelId,targetNumber);
        }

        roomMapper.updateEntityFromDto(request, room);

        if (request.getHotelId() != null) {
            Hotel newHotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new HotelNotFoundException(request.getHotelId()));

            room.setHotel(newHotel);
        }

        Room updatedRoom = roomRepository.save(room);

        return  roomMapper.toResponse(updatedRoom);
    }

    public void delete(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(()-> new RoomNotFoundException(id));

        roomRepository.delete(room);
    }

    public List<RoomResponse> findAll() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    public RoomListResponse findAll(RoomFilter filter, Pageable pageable) {

        Specification<Room> spec = RoomSpecifications.withFilter(filter);
        Page<Room> roomPage = roomRepository.findAll(spec, pageable);

        return new RoomListResponse(
                roomPage.getContent().stream()
                        .map(roomMapper::toResponse)
                        .toList(),
                roomPage.getTotalElements()
        );
    }
}
