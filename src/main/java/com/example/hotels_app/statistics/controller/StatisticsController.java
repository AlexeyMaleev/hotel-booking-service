package com.example.hotels_app.statistics.controller;

import com.example.hotels_app.model.statistics.UserActionLog;
import com.example.hotels_app.statistics.repository.HotelStatisticsRepository;
import com.example.hotels_app.statistics.service.CsvExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(
        name = "Statistics",
        description = "Операции со статистикой"
)
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final HotelStatisticsRepository statisticsRepository;

    private final CsvExportService csvExportService;

    @Operation(
            summary = "Получить статистику",
            description = "Возвращает статистику регистрации пользователей, и бронирования комнат." +
                    " Доступно авторизованным только с ролью ADMIN"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Возвращена статистика"
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
    })
    @GetMapping("/download")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Flux<String>> downloadStatistics(){

        Flux<UserActionLog> logs = statisticsRepository.findAll();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment, filename=statistics.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvExportService.getStatisticCsv(logs));
    }
}
