package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = ItemMapper.class)
public interface BookingMapper {
    @Mapping(source = "booker", target = "booker")
    @Mapping(source = "item", target = "item")
    BookingDtoOut mapToBookingDto(Booking booking);

    @Mapping(source = "itemId", target = "item.id")
    Booking mapToBooking(BookingDtoIn bookingDtoIn);
}
