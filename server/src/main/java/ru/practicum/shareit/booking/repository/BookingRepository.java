package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.emun.BookingState;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findAllByBookerIdAndEndAfterOrderByStartDesc(Long id, LocalDateTime time, Pageable page);

    Page<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long id, LocalDateTime time, Pageable page);

    Page<Booking> findAllByBookerIdAndStartBeforeOrderByStartDesc(Long id, LocalDateTime time, Pageable page);

    Page<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long id, BookingStatus status, Pageable page);

    Page<Booking> findAllByBookerId(Long id, Pageable page);

    List<Booking> findAllByItemIdInOrderByStartDesc(List<Long> ids);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long id);

    List<Booking> findAllByBookerIdAndItemIdAndEndBefore(Long userId, Long itemId, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long id, BookingState state);

    Optional<Booking> findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(Long id, LocalDateTime end, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);
}
