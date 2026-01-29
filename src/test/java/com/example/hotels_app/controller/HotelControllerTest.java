package com.example.hotels_app.controller;

import com.example.hotels_app.PostgresBaseTest;
import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.entity.Room;
import com.example.hotels_app.model.request.CreateHotelRequest;
import com.example.hotels_app.model.request.UpdateHotelRequest;
import com.example.hotels_app.model.request.UpsertRatingRequest;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class HotelControllerTest extends PostgresBaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @BeforeEach
    void clear() {
        hotelRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given valid hotel data
        When authorized admin requests to create hotel
        Then status is 201 Created
         and hotel is saved in database
        """)
    void whenCreateAsAdmin_thenReturnCreated() throws Exception {
        CreateHotelRequest request = new CreateHotelRequest(
                "Grand Hotel", "Luxury Stay", "Moscow", "Red Square 1", 5L);

        mockMvc.perform(post("/api/v1/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grand Hotel"))
                .andExpect(jsonPath("$.city").value("Moscow"));

        assertEquals(1, hotelRepository.count());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given hotel exists in database
        When authorized admin requests to update hotel
        Then status is 200 OK
         and hotel data is updated in database
        """)
    void whenUpdateAsAdmin_thenReturnOk() throws Exception {
        Hotel hotel = saveHotel("Old Name", "Five stars hotel","Saint Petersburg","Orehovskaya 21", 7L, 4.0, 1);
        UpdateHotelRequest updateRequest = new UpdateHotelRequest();
        updateRequest.setName("New Name");
        assertEquals(1, hotelRepository.count());

        mockMvc.perform(put("/api/v1/hotels/{id}", hotel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.city").value("Saint Petersburg"))
                .andExpect(jsonPath("$.rating").value(4.0));

        assertEquals(1, hotelRepository.count());

    }

    @ParameterizedTest(name = "Test {index}: get hotel by id with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given hotel exists in database
        When authorized user with valid role requests hotel by id
        Then status is 200 OK
         and response contains correct hotel data
        """)
    void whenGetById_thenReturnHotel(String role) throws Exception {
        Hotel hotel = saveHotel("Test Hotel", "Welcome!","London","Lennona street 19", 12L);

        Room room = new Room();
        room.setName("Deluxe Room");
        room.setNumber(101);
        room.setPrice(new java.math.BigDecimal("500.00"));
        room.setCapacity(2);
        room.setHotel(hotel); // Устанавливаем связь
        roomRepository.save(room);

        mockMvc.perform(get("/api/v1/hotels/{id}", hotel.getId())
                        .with(user("userName").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(hotel.getId()))
                .andExpect(jsonPath("$.name").value("Test Hotel"))
                .andExpect(jsonPath("$.rooms").isArray())
                .andExpect(jsonPath("$.rooms[0].name").value("Deluxe Room"))
                .andExpect(jsonPath("$.rooms[0].number").value(101));
    }

    @ParameterizedTest(name = "Test {index}: get all hotels with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given multiple hotels exist in database
        When authorized user with valid role requests all hotels
        Then status is 200 OK
         and list contains all saved hotels
        """)
    void whenGetAllHotelsWithValidRole_thnReturnHotelList(String role) throws Exception {
        saveHotel("Hotel 1", "Title 1", "City 1", "Avenue 1", 7L);
        saveHotel("Hotel 2", "Title 2", "City 2", "Avenue 2", 8L);

        assertEquals(2, hotelRepository.count());

        mockMvc.perform(get("/api/v1/hotels")
                        .with(user("userName").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("""
        Given hotel exists in database
        When authorized admin requests to delete hotel by id
        Then status is 204 No Content
         and hotel is removed from database
        """)
    void whenDeleteAsAdmin_thenReturnNoContent() throws Exception {
        Hotel hotel = saveHotel("To delete", "Welcome !","Citi","Lennina 42", 3L);

        assertTrue(hotelRepository.findById(hotel.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/hotels/{id}", hotel.getId()))
                .andExpect(status().isNoContent());

        assertTrue(hotelRepository.findById(hotel.getId()).isEmpty());
    }

    @ParameterizedTest(name = "Test {index}: set new rating to hotel with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given hotel exists with initial rating
        When authorized user submits new mark
        Then status is 200 OK
         and hotel rating is recalculated correctly
        """)
    void whenSetNewRateWithValidUser_thenRatingUpdated(String role) throws Exception {

        Hotel hotel = saveHotel("Rating Hotel",
                "Bests rating!",
                "Chelyabinsk",
                "Kurchatova 37",
                9L,
                4.0,
                3);

        UpsertRatingRequest ratingRequest = new UpsertRatingRequest(3);

        mockMvc.perform(put("/api/v1/hotels/{id}/rate", hotel.getId())
                        .with(user("valiUser").roles(role))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3.7))
                .andExpect(jsonPath("$.ratingCount").value(4));
    }

    @ParameterizedTest(name = "Test {index}: filter hotels with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given multiple hotels in different cities
        When user filters by city and rating
        Then status is 200 OK
         and only matching hotels are returned
        """)
    void whenFilterHotelsWithValidUserRoles_thenFilteredListReturned(String role) throws Exception {
        // Given
        saveHotel("Moscow Plaza", "Plaza title","Moscow", "Stachek 95", 12L, 4.0,  7);
        saveHotel("Moscow Economy", "Economy title","Moscow", "Chehova 112",23L, 2.0, 14);
        saveHotel("London Bridge", "Bridge title", "London", "Bitlth 16",1L, 5.0, 128);

        assertEquals(3,hotelRepository.count()) ;


        mockMvc.perform(get("/api/v1/hotels/filter")
                        .param("city", "Moscow")
                        .param("rating", "4.0")
                        .param("page", "0")
                        .param("size", "10")
                        .with(user("valiUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotels.length()").value(1))
                .andExpect(jsonPath("$.hotels[0].name").value("Moscow Plaza"));
    }

    @ParameterizedTest(name = "Test {index}: filter hotels with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given many hotels
        When user requests second page with size 1
        Then status is 200 OK
         and correct page is returned
        """)
    void filterHotels_Pagination_ShouldReturnCorrectPage(String role) throws Exception {
        // Given
        saveHotel("Hotel 1", "Title 1", "City", "Street 1", 5L);
        saveHotel("Hotel 2", "Title 2", "City", "Street 2", 12L);

        mockMvc.perform(get("/api/v1/hotels/filter")
                        .param("page", "1")
                        .param("size", "1")
                        .param("city", "City")
                        .with(user("valiUser").roles(role)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotels.length()").value(1))
                .andExpect(jsonPath("$.hotels[0].name").value("Hotel 2"));
    }

    //--- -----------------------NEGATIVE TESTS-----------------------------------------

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given valid hotel data
        When authorized user without admin role requests to create hotel
        Then status is 403 Forbidden
        """)
    void whenCreateAsUser_thnForbidden() throws Exception {
        CreateHotelRequest request = new CreateHotelRequest(
                "Grand Hotel", "Luxury Stay", "Moscow", "Red Square 1", 5L);

        mockMvc.perform(post("/api/v1/hotels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest(name = "Test {index}: get hotel by id with role {0}")
    @ValueSource(strings = {"USER", "ADMIN"})
    @DisplayName("""
        Given hotel exists in database
        When authorized user with valid role requests hotel by id
        Then status is 200 OK
         and response contains correct hotel data
        """)
    void whenGetByIdAsAsAnonymous_thenUnauthorized() throws Exception {
        Hotel hotel = saveHotel("Test Hotel", "Welcome!","London","Lennona street 19", 12L);
        assertTrue(hotelRepository.findById(hotel.getId()).isPresent());

        mockMvc.perform(get("/api/v1/hotels/{id}", hotel.getId()))
                .andExpect(status().isUnauthorized());;
    }

    @Test
    @DisplayName("""
        Given anonymous user
        When request to get all hotels is sent
        Then status is 401 Unauthorized
        """)
    void whenGetAllHotelsAsAnonymous_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/hotels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given hotel exists in database
        When authorized user requests to delete hotel by id
        Then status is 403 Forbidden
        """)
    void whenDeleteWithoutAdminRole_thenForbidden() throws Exception {
        Hotel hotel = saveHotel("To delete", "Welcome !","Citi","Lennina 42", 3L);

        assertTrue(hotelRepository.findById(hotel.getId()).isPresent());

        mockMvc.perform(delete("/api/v1/hotels/{id}", hotel.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("""
        Given hotel exists
        When user submits mark out of range 1-5
        Then status is 400 Bad Request
        """)
    void whenUpdateRateWithInvalidMark_thenBadRequest() throws Exception {
        UpsertRatingRequest invalidRequest = new UpsertRatingRequest(10);

        mockMvc.perform(put("/api/v1/hotels/1/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("""
        Given hotel exists with initial rating
        When unauthorized user submits new mark
        Then status is 401 Unauthorized
        """)
    void whenSetNewRateAsAnonymous_thenUnauthorized() throws Exception {

        Hotel hotel = saveHotel("Rating Hotel",
                "Bests rating!",
                "Chelyabinsk",
                "Kurchatova 37",
                9L,
                4.0,
                3);

        UpsertRatingRequest ratingRequest = new UpsertRatingRequest(3);

        mockMvc.perform(put("/api/v1/hotels/{id}/rate", hotel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ratingRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("""
        Given multiple hotels in different cities
        When unauthorized user submits new mark
        Then status is 401 Unauthorized
        """)
    void whenFilterHotelsAsAnonymous_thenUnauthorized() throws Exception {
        saveHotel("Moscow Plaza", "Plaza title","Moscow", "Stachek 95", 12L, 4.0,  7);
        saveHotel("Moscow Economy", "Economy title","Moscow", "Chehova 112",23L, 2.0, 14);
        saveHotel("London Bridge", "Bridge title", "London", "Bitlth 16",1L, 5.0, 128);

        assertEquals(3,hotelRepository.count());

        mockMvc.perform(get("/api/v1/hotels/filter")
                        .param("city", "Moscow")
                        .param("rating", "4.0")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isUnauthorized());
    }


    private Hotel saveHotel(String name, String title, String city, String address, Long distance) {
       return saveHotel(name, title,  city, address, distance, null, null);
    }

    private Hotel saveHotel(String name, String title, String city, String address, Long distance, Double rating, Integer ratingCount) {
        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setTitle(title);
        hotel.setCity(city);
        hotel.setAddress(address);
        hotel.setDistance(distance);
        hotel.setRating(rating);
        hotel.setRatingCount(ratingCount);
        return hotelRepository.save(hotel);
    }
}