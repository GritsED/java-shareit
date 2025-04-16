package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

public interface BookingService {
    BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, long userId);

    BookingDtoOut approve(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut findById(Long userId, Long bookingId);
}
