package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
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
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final ItemRepository itemRepository;

    @Override
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, long userId) {
        User user = checkUser(userId);
        Long itemId = bookingDtoIn.getItemId();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с Id " + itemId + " не найден"));
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет с Id " + item.getId() + " не доступен для бронирования");
        }
        Booking booking = bookingMapper.mapToBooking(bookingDtoIn);
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDtoOut approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = checkBooking(bookingId);
        Long itemOwnerId = booking.getItem().getOwner().getId();

        if (!itemOwnerId.equals(userId)) {
            throw new ValidationException("User с Id " + itemOwnerId + " не является владельцем предмета " + booking.getItem().getId());
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование подтверждено или отклонено");
        }

        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        bookingRepository.save(booking);
        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDtoOut findById(Long bookingId, Long userId) {
        checkUser(userId);
        Booking foundBooking = checkBooking(bookingId);
        return bookingMapper.mapToBookingDto(foundBooking);
    }

    @Override
    public List<BookingDtoOut> findAll(Long userId, String state) {
        checkUser(userId);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state.toUpperCase()) {
            case "CURRENT" -> bookingRepository.findAllByBookerIdAndEndAfterOrderByStartDesc(userId, now);
            case "PAST" -> bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case "FUTURE" -> bookingRepository.findAllByBookerIdAndStartBeforeOrderByStartDesc(userId, now);
            case "WAITING" ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case "REJECTED" ->
                    bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingRepository.findAllByBookerId(userId);
        };

        return bookingMapper.mapToBookingDto(bookings);
    }

    @Override
    public List<BookingDtoOut> findAllByItemOwner(Long userId, String state) {
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
        } else {
            bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, status);
        }

        if (bookings == null || bookings.isEmpty()) {
            throw new NotFoundException("У вас нет предметов для аренды");
        }

        return bookingMapper.mapToBookingDto(bookings);
    }

    private User checkUser(Long id) {
        return userService.findUser(id);
    }

    private Booking checkBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Бронирование с Id " + id + " не найдено"));
    }


}
