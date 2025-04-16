package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public class BookingDtoIn {
    Long itemId;
    LocalDateTime start;
    LocalDateTime end;

    public Long getItemId() {
        return itemId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
