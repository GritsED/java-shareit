package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.emun.BookingState;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, long userId) {
        log.debug("Пытаемся создать бронирование. userId={}, itemId={}", userId, bookingDtoIn.getItemId());

        User user = checkUser(userId);
        Long itemId = bookingDtoIn.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с Id " + itemId + " не найден"));
        if (!item.getAvailable()) {
            log.warn("Предмет с Id {} недоступен для бронирования пользователем {}", itemId, userId);
            throw new ValidationException("Предмет с Id " + item.getId() + " не доступен для бронирования");
        }
        Booking booking = bookingMapper.mapToBooking(bookingDtoIn);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        Booking save = bookingRepository.save(booking);
        log.debug("Бронирование создано успешно. bookingId={}", save.getId());
        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDtoOut approve(Long userId, Long bookingId, Boolean approved) {
        log.debug("Пользователь {} пытается изменить статус бронирования {} на {}",
                userId, bookingId, approved ? "APPROVED" : "REJECTED");
        Booking booking = checkBooking(bookingId);
        Long itemOwnerId = booking.getItem().getOwner().getId();

        if (!itemOwnerId.equals(userId)) {
            log.warn("Пользователь {} не является владельцем предмета {} для бронирования {}", userId, booking.getItem().getId(), bookingId);
            throw new ValidationException("User с Id " + itemOwnerId + " не является владельцем предмета " + booking.getItem().getId());
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            log.warn("Бронирование {} уже подтверждено или отклонено", bookingId);
            throw new ValidationException("Бронирование подтверждено или отклонено");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
            log.debug("Бронирование {} подтверждено", bookingId);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
            log.debug("Бронирование {} отклонено", bookingId);
        }

        bookingRepository.save(booking);
        log.debug("Статус бронирования {} успешно обновлен", bookingId);
        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDtoOut findById(Long bookingId, Long userId) {
        log.debug("Пользователь {} запрашивает бронирование с Id {}", userId, bookingId);
        checkUser(userId);
        Booking foundBooking = checkBooking(bookingId);
        return bookingMapper.mapToBookingDto(foundBooking);
    }

    @Override
    public List<BookingDtoOut> findAll(Long userId, String state, Integer from, Integer size) {
        log.debug("Пользователь {} запрашивает бронирования с состоянием '{}', from={}, size={}",
                userId, state, from, size);
        checkUser(userId);

        LocalDateTime now = LocalDateTime.now();

        Pageable page = PageRequest.of(from > 0 ? from / size : 0, size);

        Page<Booking> bookings = switch (state.toUpperCase()) {
            case "CURRENT" -> bookingRepository.findAllByBookerIdAndEndAfterOrderByStartDesc(userId, now, page);
            case "PAST" -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now, page);
            case "FUTURE" -> bookingRepository.findAllByBookerIdAndStartBeforeOrderByStartDesc(userId, now, page);
            case "WAITING" ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING, page);
            case "REJECTED" ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED, page);
            default -> bookingRepository.findAllByBookerId(userId, page);
        };

        if (bookings.isEmpty()) {
            log.warn("Пользователь {} не найдено бронирований с состоянием '{}'", userId, state);
        } else {
            log.debug("Найдено {} бронирований для пользователя {} с состоянием '{}'",
                    bookings.getTotalElements(), userId, state);
        }

        return bookings.map(bookingMapper::mapToBookingDto).getContent();
    }

    @Override
    public List<BookingDtoOut> findAllByItemOwner(Long userId, String state) {
        log.debug("Пользователь {} запрашивает бронирования предметов со статусом '{}'", userId, state);
        checkUser(userId);

        BookingState status = switch (state.toUpperCase()) {
            case "CURRENT" -> BookingState.CURRENT;
            case "PAST" -> BookingState.PAST;
            case "FUTURE" -> BookingState.FUTURE;
            case "WAITING" -> BookingState.WAITING;
            case "REJECTED" -> BookingState.REJECTED;
            default -> null;
        };

        List<Booking> bookings;

        if (status == null) {
            bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId);
            log.debug("Статус не указан или некорректен, выбраны все бронирования для владельца {}", userId);
        } else {
            bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, status);
            log.debug("Выбраны бронирования для владельца {} со статусом {}", userId, status);
        }

        if (bookings == null || bookings.isEmpty()) {
            log.warn("У пользователя {} нет предметов для аренды", userId);
            throw new NotFoundException("У вас нет предметов для аренды");
        } else {
            log.debug("Найдено {} бронирований для пользователя {}", bookings.size(), userId);
        }

        return bookingMapper.mapToBookingDto(bookings);
    }

    private User checkUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    private Booking checkBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с Id " + id + " не найдено"));
    }
}
