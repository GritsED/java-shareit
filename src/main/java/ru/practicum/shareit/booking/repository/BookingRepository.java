package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.emun.BookingState;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdAndEndAfterOrderByStartDesc(Long id, LocalDateTime time);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long id, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStartBeforeOrderByStartDesc(Long id, LocalDateTime time);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long id, BookingStatus status);

    List<Booking> findAllByBookerId(Long id);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long id);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long id, BookingState state);
}
