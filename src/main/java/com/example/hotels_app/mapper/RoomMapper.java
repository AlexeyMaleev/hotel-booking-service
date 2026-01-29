package com.example.hotels_app.mapper;

import com.example.hotels_app.entity.Room;
import com.example.hotels_app.model.request.CreateRoomRequest;
import com.example.hotels_app.model.request.UpdateRoomRequest;
import com.example.hotels_app.model.response.RoomResponse;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    Room toEntity(CreateRoomRequest request);

    @Mapping(target = "hotelId", source = "hotel.id")
    RoomResponse toResponse(Room room);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(UpdateRoomRequest request,
                             @MappingTarget Room room);
}
