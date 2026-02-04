package com.example.hotels_app.controller;

import com.example.hotels_app.PostgresBaseTest;
import com.example.hotels_app.entity.Booking;
import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.entity.User;
import com.example.hotels_app.model.request.CreateBookingRequest;
import com.example.hotels_app.model.request.UpdateBookingRequest;
import com.example.hotels_app.repository.BookingRepository;
import com.example.hotels_app.repository.HotelRepository;
import com.example.hotels_app.repository.RoomRepository;
import com.example.hotels_app.repository.UserRepository;
import com.example.hotels_app.security.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class BookingControllerTest extends PostgresBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User testUser;
    private Room testRoom;

    private static final int BOOKING_YEAR = LocalDate.now().getYear() + 1;

    @BeforeEach
    void setUp(){

        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setName("Booking User");
        testUser.setEmail("book@test.com");
        testUser.setPassword("123");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        Hotel hotel = new Hotel();
        hotel.setName("Booking Hotel");
        hotel.setTitle("Booking title");
        hotel.setCity("Moscow");
        hotel.setAddress("Booking street 12");
        hotel.setDistance(3L);
        hotel.setRating(3.0);
        hotel.setRatingCount(749);
        hotel = hotelRepository.save(hotel);

        testRoom = new Room();
        testRoom.setName("Room 1");
        testRoom.setNumber(12);
        testRoom.setPrice(BigDecimal.valueOf(1000));
        testRoom.setDescription("Description");
        testRoom.setCapacity(2);
        testRoom.setHotel(hotel);

        testRoom = roomRepository.save(testRoom);
    }

    @ParameterizedTest(name = "Test {index}: create booking with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given valid booking data
        When authorized user requests to create booking
        Then status is 201 Created
         and booking is saved in database
         and notification is sent to Kafka
        """)
    void whenCreateBooking_thenReturnCreated(String role) throws Exception {

        CreateBookingRequest request = new CreateBookingRequest(
                LocalDate.of(BOOKING_YEAR, 2, 1),
                LocalDate.of(BOOKING_YEAR, 2, 10),
                testUser.getId(),
                testRoom.getId()
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .with(user("book_user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(testRoom.getId()))
                .andExpect(jsonPath("$.userName").value(testUser.getName()))
                .andExpect(jsonPath("$.roomNumber").value(testRoom.getNumber()));

        assertEquals(1, bookingRepository.count());

        verify(kafkaTemplate, times(1)).send(any(), any());
    }

    @ParameterizedTest(name = "Test {index}: update booking with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given booking exists
        When user with valid role requests to update dates
        Then status is 200 OK
         and dates are updated in database
        """)
    void whenUpdateBookingWithValidRole_thenReturnOk(String role) throws Exception {

        Booking booking = saveBooking(
                LocalDate.of(BOOKING_YEAR, 3, 1),
                LocalDate.of(BOOKING_YEAR, 3, 5));

        assertEquals(1, bookingRepository.count());

        UpdateBookingRequest updateRequest = new UpdateBookingRequest(
                LocalDate.of(BOOKING_YEAR, 3, 2),
                LocalDate.of(BOOKING_YEAR, 3, 6),
                testRoom.getId()
        );

        mockMvc.perform(put("/api/v1/bookings/{id}", booking.getId())
                        .with(user("book_user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.arrival").value( BOOKING_YEAR+"-03-02"))
                .andExpect(jsonPath("$.departure").value(BOOKING_YEAR+"-03-06"));

        assertEquals(1, bookingRepository.count());
    }

    @ParameterizedTest(name = "Test {index}: find booking by id with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given booking exists
        When authorized user with valid role requests booking by id
        Then status is 200 OK
         and response contains correct data
        """)
    void whenGetByIdWithUserValidRole_thenReturnBooking(String role) throws Exception {
        Booking booking = saveBooking(
                LocalDate.of(BOOKING_YEAR, 3, 1),
                LocalDate.of(BOOKING_YEAR, 3, 5)
        );

        assertEquals(1, bookingRepository.count());

        mockMvc.perform(get("/api/v1/bookings/{id}", booking.getId())
                        .with(user("any_user").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.userName").value(testUser.getName()));
    }

    @ParameterizedTest(name = "Test {index}: delete booking with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given booking exists
        When authorized user with valid role requests to delete booking
        Then status is 204 No Content
         and booking is removed from database
        """)
    void whenDeleteWithValidRole_thenReturnNoContent(String role) throws Exception {
        Booking booking = saveBooking(
                LocalDate.of(BOOKING_YEAR, 3, 1),
                LocalDate.of(BOOKING_YEAR, 3, 5)
        );

        assertTrue(bookingRepository.findById(booking.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/bookings/{id}", booking.getId())
                        .with(user("admin").roles(role)))
                .andExpect(status().isNoContent());

        assertFalse(bookingRepository.findById(booking.getId()).isPresent());
    }

    // --------------------NEGATIVE TESTS----------------------------

    @Test
    @DisplayName("""
        Given room is already booked for dates
        When user requests to book overlapping dates
        Then status is 400 Bad Request
        """)
    void whenCreateBookingWithOverlappingDates_thenBadRequest() throws Exception {
        saveBooking(
                LocalDate.of(BOOKING_YEAR, 2, 5),
                LocalDate.of(BOOKING_YEAR, 2, 10)
        );

        CreateBookingRequest request = new CreateBookingRequest(
                LocalDate.of(BOOKING_YEAR, 2, 7),
                LocalDate.of(BOOKING_YEAR, 2, 12),
                testUser.getId(),
                testRoom.getId()
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .with(user("another_user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        "already busy in that period")))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(
                        testRoom.getId().toString())));

        assertEquals(1, bookingRepository.count());
    }

    @Test
    @DisplayName("""
        Given authorized user with ROLE_USER
        When request to get all bookings
        Then status is 403 Forbidden
        """)
    void whenGetAllAsUser_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/bookings")
                        .with(user("simple_user").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("""
        Given valid booking data
        When unauthorized user requests to create booking
        Then status is 401 Unauthorized
        """)
    void whenUnauthorizedUserCreate_thenUnauthorized() throws Exception {
        CreateBookingRequest request = new CreateBookingRequest(
                LocalDate.of(BOOKING_YEAR, 2, 1),
                LocalDate.of(BOOKING_YEAR, 2, 10),
                testUser.getId(),
                testRoom.getId()
        );

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given valid booking data
        When unauthorized user requests to update booking
        Then status is 401 Unauthorized
        """)
    void whenUnauthorizedUserUpdate_thenUnauthorized() throws Exception {
        Booking booking = saveBooking(
                LocalDate.of(BOOKING_YEAR, 3, 1),
                LocalDate.of(BOOKING_YEAR, 3, 5));

        assertEquals(1, bookingRepository.count());

        UpdateBookingRequest updateRequest = new UpdateBookingRequest(
                LocalDate.of(BOOKING_YEAR, 3, 2),
                LocalDate.of(BOOKING_YEAR, 3, 6),
                testRoom.getId()
        );

        mockMvc.perform(put("/api/v1/bookings/{id}", booking.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given booking exists
        When authorized user with valid role requests to delete booking
        Then status is 204 No Content
         and booking is removed from database
        """)
    void whenDeleteAsUnauthorizedUser_thenUnauthorized() throws Exception {
        Booking booking = saveBooking(
                LocalDate.of(BOOKING_YEAR, 3, 1),
                LocalDate.of(BOOKING_YEAR, 3, 5)
        );

        assertTrue(bookingRepository.findById(booking.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/bookings/{id}", booking.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(bookingRepository.findById(booking.getId()).isPresent());
    }

    private Booking saveBooking(LocalDate arrival, LocalDate departure){
        Booking booking = new Booking();
        booking.setUser(testUser);
        booking.setRoom(testRoom);
        booking.setArrival(arrival.atStartOfDay(ZoneOffset.UTC).toInstant());
        booking.setDeparture(departure.atStartOfDay(ZoneOffset.UTC).toInstant());
        return bookingRepository.save(booking);
    }
}