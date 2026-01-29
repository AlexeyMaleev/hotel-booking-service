package com.example.hotels_app.controller;

import com.example.hotels_app.PostgresBaseTest;
import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.model.request.CreateRoomRequest;
import com.example.hotels_app.model.request.UpdateRoomRequest;
import com.example.hotels_app.repository.HotelRepository;
import com.example.hotels_app.repository.RoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class RoomControllerTest extends PostgresBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HotelRepository hotelRepository;

    private Long hotelId;

    @BeforeEach
    void setUp() {
        roomRepository.deleteAll();
        hotelRepository.deleteAll();

        Hotel hotel = new Hotel();
        hotel.setName("Grand Palace");
        hotel.setTitle("Luxury Stay");
        hotel.setCity("Moscow");
        hotel.setAddress("Red Square 1");
        hotel.setDistance(0L);
        hotel = hotelRepository.save(hotel);
        this.hotelId = hotel.getId();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given valid room data
        When authorized admin requests to create room
        Then status is 201 Created
         and room is linked to correct hotel
        """)
    void whenCreateRoomAsAdmin_thenCreated() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest(
                "Deluxe Room",
                "King size bed, sea view",
                101,
                new BigDecimal("15000.00"),
                2,
                hotelId
        );

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Deluxe Room"))
                .andExpect(jsonPath("$.number").value(101));

        assertEquals(1, roomRepository.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given room exists in database
        When authorized admin requests to update room
        Then status is 200 OK
         and room name is updated in database
        """)
    void whenUpdateAsAdmin_thnOk() throws Exception {
        Room room = saveRoom("Old Name", 303, new BigDecimal("7000.00"));
        UpdateRoomRequest updateRequest = new UpdateRoomRequest();
        updateRequest.setName("New Name");

        mockMvc.perform(put("/api/v1/rooms/{id}", room.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"));
    }

    @ParameterizedTest(name = "Test {index}: get room by id with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given room exists in database
        When authorized user requests room by id
        Then status is 200 OK
         and response contains correct room data
        """)
    void whenGetById_thenReturnRoom(String role) throws Exception {
        Room room = saveRoom("Standard", 202, new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/v1/rooms/{id}", room.getId())
                        .with(user("user").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Standard"))
                .andExpect(jsonPath("$.number").value(202));
    }

    @ParameterizedTest(name = "Test {index}: get all room with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given rooms exists in database
        When authorized user requests all rooms
        Then status is 200 OK
         and response contains correct rooms data list.
        """)
    void whenGetAllWithValidRole_thenReturnRoomList(String role) throws Exception {
        saveRoom("Standard", 202, new BigDecimal("5000.00"));
        saveRoom("Luxe", 74, new BigDecimal("15000.00"));
        saveRoom("Apartment", 96, new BigDecimal("1000.00"));

        assertEquals(3, roomRepository.count());

        mockMvc.perform(get("/api/v1/rooms")
                        .with(user("user").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Standard"))
                .andExpect(jsonPath("$[0].number").value(202))
                .andExpect(jsonPath("$[1].name").value("Luxe"))
                .andExpect(jsonPath("$[1].number").value(74))
                .andExpect(jsonPath("$[2].name").value("Apartment"))
                .andExpect(jsonPath("$[2].number").value(96));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given room exists in database
        When authorized admin requests to delete room
        Then status is 204 No Content
         and room is removed from database
        """)
    void whenDeleteAsAdmin_thenReturnNoContent() throws Exception {
        Room room = saveRoom("To Delete", 404, new BigDecimal("1000.00"));
        assertTrue(roomRepository.findById(room.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/rooms/{id}", room.getId()))
                .andExpect(status().isNoContent());

        assertTrue(roomRepository.findById(room.getId()).isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given valid room data
        When authorized user without admin role requests to create room
        Then status is 403 Forbidden
        """)
    void whenCreateAsUser_thenForbidden() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("Fail", "Desc", 1, BigDecimal.TEN, 1, hotelId);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given room exists in database
        When authorized user without admin role requests to update room
        Then status is 403 Forbidden
        """)
    void whenUpdateAsUser_thenForbidden() throws Exception {
        Room room = saveRoom("Old Name", 303, new BigDecimal("7000.00"));
        UpdateRoomRequest updateRequest = new UpdateRoomRequest();
        updateRequest.setName("New Name");

        mockMvc.perform(put("/api/v1/rooms/{id}", room.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given room exists in database
        When authorized user without admin role requests to delete room
        Then status is 403 Forbidden
        """)
    void whenDeleteAsUser_thenForbidden() throws Exception {
        Room room = saveRoom("Old Name", 303, new BigDecimal("7000.00"));
        assertTrue(roomRepository.findById(room.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/rooms/{id}", room.getId()))
                .andExpect(status().isForbidden());

        assertTrue(roomRepository.findById(room.getId()).isPresent());
    }

    @Test
    @DisplayName("""
        Given valid room data
        When unauthorized user requests to create room
        Then status is 401 Unauthorized
        """)
    void whenCreateAsUnauthorized_thenUnauthorized() throws Exception {
        CreateRoomRequest request = new CreateRoomRequest("Fail", "Desc", 1, BigDecimal.TEN, 1, hotelId);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given room exists in database
        When unauthorized user requests to update room
        Then status is 401 Unauthorized
        """)
    void whenUpdateAsUnauthorized_thenUnauthorized() throws Exception {
        Room room = saveRoom("Old Name", 303, new BigDecimal("7000.00"));
        UpdateRoomRequest updateRequest = new UpdateRoomRequest();
        updateRequest.setName("New Name");

        mockMvc.perform(put("/api/v1/rooms/{id}", room.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given room exists in database
        When authorized user requests to delete room
        Then status is 401 Unauthorized
        """)
    void whenDeleteAsUnauthorized_thenUnauthorized() throws Exception {
        Room room = saveRoom("Old Name", 303, new BigDecimal("7000.00"));
        assertTrue(roomRepository.findById(room.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/rooms/{id}", room.getId()))
                .andExpect(status().isUnauthorized());

        assertTrue(roomRepository.findById(room.getId()).isPresent());
    }

    @Test
    @DisplayName("""
        Given room exists in database
        When authorized user requests room by id
        Then status is 401 Unauthorized
        """)
    void whenGetByIdAsUnauthorized_thenUnauthorized() throws Exception {
        Room room = saveRoom("Standard", 202, new BigDecimal("5000.00"));
        assertEquals(1, roomRepository.count());

        mockMvc.perform(get("/api/v1/rooms/{id}", room.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given rooms exists in database
        When unauthorized user requests all rooms
        Then status is 401 Unauthorized
        """)
    void whenGetAllAsUnauthorized_thenUnauthorized() throws Exception {
        saveRoom("Standard", 202, new BigDecimal("5000.00"));
        saveRoom("Luxe", 74, new BigDecimal("15000.00"));
        saveRoom("Apartment", 96, new BigDecimal("1000.00"));

        assertEquals(3, roomRepository.count());

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
    Given room with number 202 already exists in hotel
    When authorized admin requests to create room with same number in same hotel
    Then status is 409 Conflict
     and error message indicates duplicate room number
    """)
    void whenCreateRoomWithExistingNumber_thenConflict() throws Exception {
        saveRoom("Standard", 202, new BigDecimal("5000.00"));
        long countBefore = roomRepository.count();

        CreateRoomRequest request = new CreateRoomRequest(
                "New Room",
                "Description",
                202, // Дубликат
                new BigDecimal("6000.00"),
                2,
                hotelId
        );

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));

        assertEquals(countBefore, roomRepository.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
    Given two rooms exist in hotel
    When authorized admin requests to update first room with number of second room
    Then status is 409 Conflict
     and room data in database remains unchanged
    """)
    void whenUpdateRoomToExistingNumber_thenConflict() throws Exception {
        Room roomToUpdate = saveRoom("Room 1", 101, new BigDecimal("3000.00"));
        saveRoom("Room 2", 202, new BigDecimal("5000.00")); // Номер, на который будем натыкаться

        UpdateRoomRequest request = new UpdateRoomRequest();
        request.setNumber(202); // Конфликтный номер
        request.setHotelId(hotelId);
        request.setName("Updated Name");
        request.setPrice(new BigDecimal("4000.00"));

        mockMvc.perform(put("/api/v1/rooms/{id}", roomToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already exists")));

        Room unchangedRoom = roomRepository.findById(roomToUpdate.getId()).orElseThrow();

        assertEquals(101, unchangedRoom.getNumber());
    }

    private Room saveRoom(String name, Integer number, BigDecimal price) {
        Room room = new Room();
        room.setName(name);
        room.setNumber(number);
        room.setPrice(price);
        room.setDescription("Description");
        room.setCapacity(2);

        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow();
        room.setHotel(hotel);

        return roomRepository.save(room);
    }
}