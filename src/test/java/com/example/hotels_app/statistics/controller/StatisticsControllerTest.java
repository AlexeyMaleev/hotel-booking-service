package com.example.hotels_app.statistics.controller;

import com.example.hotels_app.FullStackBaseTest;
import com.example.hotels_app.model.statistics.ActionType;
import com.example.hotels_app.model.statistics.UserActionLog;
import com.example.hotels_app.statistics.repository.HotelStatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;

class StatisticsControllerTest extends FullStackBaseTest {

    @Autowired
    private HotelStatisticsRepository statisticsRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        statisticsRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @DisplayName("""
        Given logs exist in MongoDB
        When authorized admin requests to download statistics
        Then status is 200 OK
         and response is correct CSV file
        """)
    void whenDownloadStatisticsAsAdmin_thenCsvReturned() throws Exception {

        UserActionLog log1 = new UserActionLog(
                null,
                101L,
                //"REGISTRATION",
                ActionType.REGISTRATION.name(),
                null,
                null,
                Instant.now()
        );

        UserActionLog log2 = new UserActionLog(
                null,
                102L,
                //"BOOKING",
                ActionType.BOOKING.name(),
                LocalDate.of(2026, 1, 20),
                LocalDate.of(2026, 1, 25),
                Instant.now()
        );

        statisticsRepository.saveAll(List.of(log1, log2)).collectList().block();

        Long count = statisticsRepository.count().block();
        assertEquals(2, count);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/statistics/download"))
                .andExpect(request().asyncStarted())
                .andReturn();

        String content = mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment, filename=statistics.csv"))
                .andExpect(content().contentType("text/csv"))
                .andReturn().getResponse().getContentAsString();

        String[] lines = content.split("\n");

        String expectedHeader = "ID,ActionType,UserID,Arrival,Departure,CreatedAt";
        assertEquals(expectedHeader, lines[0].trim(), "Заголовки CSV не совпадают с ожидаемыми");

//        assertTrue(content.contains("REGISTRATION,101"), "Строка регистрации сформирована неверно");
//        assertTrue(content.contains("BOOKING,102,2026-01-20,2026-01-25"), "Строка бронирования сформирована неверно");

        assertTrue(content.contains( ActionType.REGISTRATION.name()+",101"),
                "Строка регистрации сформирована неверно");
        assertTrue(content.contains(ActionType.BOOKING.name()+",102,2026-01-20,2026-01-25"),
                "Строка бронирования сформирована неверно");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    @DisplayName("""
        Given user without admin role
        When authorized user requests to download statistics
        Then status is 403 Forbidden
        """)
    void whenDownloadStatisticsAsUser_thenForbidden() throws Exception {
             mockMvc.perform(get("/api/v1/statistics/download"))
                    .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("""
        Given anonymous user
        When unauthorized user requests to download statistics
        Then status is 401 Unauthorized
        """)
    void whenDownloadStatisticsAnonymous_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/statistics/download"))
                .andExpect(status().isUnauthorized());
    }

}