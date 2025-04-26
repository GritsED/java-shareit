package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper
public interface BookingMapper {
    @Mapping(source = "booker", target = "booker")
    @Mapping(source = "item", target = "item")
    BookingDtoOut mapToBookingDto(Booking booking);

    @Mapping(source = "itemId", target = "item.id")
    Booking mapToBooking(BookingDtoIn bookingDtoIn);

    List<BookingDtoOut> mapToBookingDto(Iterable<Booking> bookings);
}
