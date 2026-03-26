package com.example.hotels_app.e2e;

import com.example.hotels_app.E2EBaseTest;
import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.entity.User;
import com.example.hotels_app.model.request.CreateBookingRequest;
import com.example.hotels_app.model.statistics.ActionType;
import com.example.hotels_app.repository.BookingRepository;
import com.example.hotels_app.repository.HotelRepository;
import com.example.hotels_app.repository.RoomRepository;
import com.example.hotels_app.security.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BookingE2ETest extends E2EBaseTest {

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
    void setUp() {

        // Очищаем таблицы в правильном порядке (от дочерних к родительским из-за Foreign Keys)
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();

        // Очищаем MongoDB
        statisticsRepository.deleteAll().block();

        testUser = new User();
        testUser.setName("Booking User");
        testUser.setEmail("book@test.com");
        testUser.setPassword("123");
        testUser.setRole(Role.USER);
        testUser = userRepository.save(testUser);

        Hotel hotel = new Hotel();
        hotel.setName("Booking Hotel");
        hotel.setTitle("Luxury Stay in Center");
        hotel.setCity("Moscow");
        hotel.setAddress("Booking street 12");
        hotel.setDistance(3L);
        hotel.setRating(3.0);
        hotel.setRatingCount(100);
        hotel = hotelRepository.save(hotel);

        testRoom = new Room();
        testRoom.setName("Room 1");
        testRoom.setNumber(12);
        testRoom.setPrice(BigDecimal.valueOf(1000));
        testRoom.setCapacity(2);
        testRoom.setHotel(hotel);
        testRoom = roomRepository.save(testRoom);
    }

    @ParameterizedTest(name = "Test {index}: create booking with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given valid booking data
        When authorized user requests to create booking via API
        Then status is 201 Created
         and booking is saved in PostgreSQL
         and "BOOKING" event is processed via Kafka
         and statistics record is saved in MongoDB with correct dates
        """)
    void whenCreateBooking_thenStatisticsSavedInMongo(String role) throws Exception {
        // 1. Prepare Request
        LocalDate arrival = LocalDate.of(BOOKING_YEAR, 2, 1);
        LocalDate departure = LocalDate.of(BOOKING_YEAR, 2, 10);

        CreateBookingRequest request = new CreateBookingRequest(
                arrival, departure, testUser.getId(), testRoom.getId()
        );

        // 2. Act (Execute API Call)
        mockMvc.perform(post("/api/v1/bookings")
                        .with(user("book_user").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(testRoom.getId()))
                .andExpect(jsonPath("$.userName").value(testUser.getName()));

        // 3. Verify PostgreSQL (Sync)
        assertEquals(1, bookingRepository.count(), "Booking should be saved in PostgreSQL");

        // 4. Verify MongoDB Integration (Async via Awaitility)
        await()
                .atMost(5, SECONDS)
                .pollInterval(500, java.util.concurrent.TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    var logs = statisticsRepository.findAll().collectList().block();
                    assertNotNull(logs, "Statistics logs list should not be null");

                    // Ищем лог, который соответствует нашему бронированию
                    boolean isLogPresent = logs.stream().anyMatch(log ->
                            log.getUserId().equals(testUser.getId()) &&
                                    ActionType.BOOKING.name().equals(log.getActionType()) &&
                                    arrival.equals(log.getArrival()) &&
                                    departure.equals(log.getDeparture())
                    );

                    assertTrue(isLogPresent,
                            String.format("Booking log not found in MongoDB for User ID: %d, Dates: %s - %s",
                                    testUser.getId(), arrival, departure));
                });
    }
}

