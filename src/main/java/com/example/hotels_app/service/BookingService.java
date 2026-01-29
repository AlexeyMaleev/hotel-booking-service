package com.example.hotels_app.service;

import com.example.hotels_app.entity.Booking;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.entity.User;
import com.example.hotels_app.exception.*;
import com.example.hotels_app.mapper.BookingMapper;
import com.example.hotels_app.model.event.BookingEvent;
import com.example.hotels_app.model.request.CreateBookingRequest;
import com.example.hotels_app.model.request.UpdateBookingRequest;
import com.example.hotels_app.model.response.BookingListResponse;
import com.example.hotels_app.model.response.BookingResponse;
import com.example.hotels_app.repository.BookingRepository;
import com.example.hotels_app.repository.RoomRepository;
import com.example.hotels_app.repository.UserRepository;
import com.example.hotels_app.statistics.service.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;

    private final UserRepository userRepository;

    private final RoomRepository roomRepository;

    private final KafkaEventProducer kafkaEventProducer;

    private final BookingMapper bookingMapper;

    public BookingResponse findById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(()-> new BookingNotFoundException(id));

        return bookingMapper.toResponse(booking);
    }

    public BookingListResponse getAll(int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        Page<Booking> bookingPage = bookingRepository.findAll(pageable);

        return new BookingListResponse(
                bookingPage.getContent()
                        .stream()
                        .map(bookingMapper::toResponse)
                        .toList(),
                bookingPage.getTotalElements());
    }


    @Transactional
    public BookingResponse create(CreateBookingRequest request){

        Instant arrival = request.getArrival().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant departure = request.getDeparture().atStartOfDay(ZoneOffset.UTC).toInstant();

        checkBookingDateOrder(departure, arrival);

        boolean isOccupied = bookingRepository.existsOverlappingBooking(
                request.getRoomId(),
                arrival,
                departure
        );

        if (isOccupied) {
            throw new RoomAlreadyBookedException( request.getRoomId(), arrival, departure);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(request.getRoomId()));

        Booking booking = bookingMapper.toEntity(request);

        booking.setUser(user);
        booking.setRoom(room);

        bookingRepository.save(booking);

        kafkaEventProducer.sendBookingEvent(
                new BookingEvent(
                        booking.getUser().getId(),
                        request.getArrival(),
                        request.getDeparture())
        );

        return bookingMapper.toResponse(booking);
    }

    @Transactional
    public BookingResponse update(Long id, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(()-> new BookingNotFoundException(id));

        Instant newArrival = request.getArrival() != null
                ? request.getArrival().atStartOfDay(ZoneOffset.UTC).toInstant()
                : booking.getArrival();

        Instant newDeparture = request.getDeparture() != null
                ? request.getDeparture().atStartOfDay(ZoneOffset.UTC).toInstant()
                : booking.getDeparture();

        Long roomId = request.getRoomId() != null
                ? request.getRoomId()
                : booking.getRoom().getId();

        checkBookingDateOrder(newDeparture, newArrival);

        boolean isOccupied = bookingRepository.existsConflictingBooking(
                roomId,
                newArrival,
                newDeparture,
                booking.getId()
        );

        if (isOccupied) {
            throw new RoomAlreadyBookedException(roomId, newArrival, newDeparture);
        }

        bookingMapper.updateEntity(request, booking);

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new RoomNotFoundException(request.getRoomId()));
            booking.setRoom(room);
        }

        bookingRepository.save(booking);

        return bookingMapper.toResponse(booking);
    }

    public void deleteById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(()-> new BookingNotFoundException(id));

        bookingRepository.delete(booking);
    }

    /*
    private Booking toEntity(CreateBookingRequest request){
        Booking booking = new Booking();

        Instant arrival = request.getArrival().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant departure = request.getDeparture().atStartOfDay(ZoneOffset.UTC).toInstant();

        booking.setArrival(arrival);
        booking.setDeparture(departure);

        booking.setUser(userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(request.getUserId())));

        booking.setRoom(roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(request.getRoomId())));

        return booking;
    }

     */

    private void checkBookingDateOrder(Instant end, Instant start){
        if(end.isBefore(start)){
            throw new BookingOrderDateException(
                    end.toString(),
                    start.toString() );
        }
    }
}
