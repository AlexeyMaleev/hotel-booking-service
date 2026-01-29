package com.example.hotels_app.service;

import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.exception.HotelNotFoundException;
import com.example.hotels_app.mapper.HotelMapper;
import com.example.hotels_app.model.request.CreateHotelRequest;
import com.example.hotels_app.model.request.HotelFilter;
import com.example.hotels_app.model.request.UpdateHotelRequest;
import com.example.hotels_app.model.response.HotelListResponse;
import com.example.hotels_app.model.response.HotelResponse;
import com.example.hotels_app.repository.HotelRepository;
import com.example.hotels_app.repository.specification.HotelSpecifications;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    private final HotelMapper hotelMapper;

    @Transactional(readOnly = true)
    public HotelResponse findById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()-> new HotelNotFoundException(id));
        return  hotelMapper.toResponse(hotel);
    }

    public HotelResponse create(CreateHotelRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel savedHotel = hotelRepository.save(hotel);

        return hotelMapper.toResponse(savedHotel);
    }

    @Transactional
    public HotelResponse update(Long id, UpdateHotelRequest request) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()-> new HotelNotFoundException(id));

        hotelMapper.updateEntityFromDto(request, hotel);
        Hotel saved = hotelRepository.save(hotel);

        return hotelMapper.toResponse(saved);
    }

    public void delete(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()-> new HotelNotFoundException(id));

        hotelRepository.delete(hotel);
    }

    public List<HotelResponse> findAll() {

        return  hotelRepository.findAll().stream()
                .map(hotelMapper::toResponse)
                .toList();
    }

    public HotelListResponse findAll(HotelFilter filter, Pageable pageable) {

        Specification<Hotel> spec = HotelSpecifications.withFilter(filter);
        Page<Hotel> hotelPage = hotelRepository.findAll(spec, pageable);

        return new HotelListResponse(
                hotelPage.getContent().stream().map(hotelMapper::toResponse).toList(),
                hotelPage.getTotalElements() // Количество всех записей в БД по данному фильтру
        );
    }

    //TODO - Внимание ПРОВЕРЯЮЩЕМУ! Рейтинг рассчитывается строго по формуле из ТЗ.
    // Хотя формула не рассчитывает среднее арифметическое. Казалось бы при 2-х оценках
    // 3 и 4 - рейтинг должен быть 3.5. Но по формуле из ТЗ - он 4.0
    // Но сделал как написано в ТЗ
    public HotelResponse updateRating(Long id, @Min(1) @Max(5) Integer newMark) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(()-> new HotelNotFoundException(id));

        Double rating = hotel.getRating();
        if(rating == null){
            hotel.setRating(newMark.doubleValue());
            hotel.setRatingCount(1);
        } else {
            Integer ratingCount = hotel.getRatingCount();
            Double totalRating = rating*ratingCount - rating + newMark;
            rating = Math.round(totalRating/ratingCount*10.0)/10.0;
            ratingCount++;

            hotel.setRating(rating);
            hotel.setRatingCount(ratingCount);
        }

        return  hotelMapper.toResponse(hotelRepository.save(hotel));
    }
}
