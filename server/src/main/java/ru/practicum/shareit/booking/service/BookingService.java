package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;

import java.util.List;

public interface BookingService {
    BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, long userId);

    BookingDtoOut approve(Long userId, Long bookingId, Boolean approved);

    BookingDtoOut findById(Long userId, Long bookingId);

    List<BookingDtoOut> findAll(Long userId, String state, Integer from, Integer size);

    List<BookingDtoOut> findAllByItemOwner(Long userId, String state);
}
