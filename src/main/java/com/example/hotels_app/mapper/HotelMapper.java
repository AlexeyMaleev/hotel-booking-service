package com.example.hotels_app.mapper;

import com.example.hotels_app.entity.Hotel;
import com.example.hotels_app.model.request.CreateHotelRequest;
import com.example.hotels_app.model.request.UpdateHotelRequest;
import com.example.hotels_app.model.response.HotelResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {RoomMapper.class})
public interface HotelMapper {

    Hotel toEntity(CreateHotelRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    void updateEntityFromDto(UpdateHotelRequest request, @MappingTarget Hotel hotel);

    HotelResponse toResponse(Hotel hotel);
}
