package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

@Service
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final ItemService itemService;
    private final ItemMapper itemMapper;

    public BookingServiceImpl(BookingRepository bookingRepository, BookingMapper bookingMapper, UserService userService, ItemService itemService, ItemMapper itemMapper) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.userService = userService;
        this.itemService = itemService;
        this.itemMapper = itemMapper;
    }

    @Override
    public BookingDtoOut createBooking(BookingDtoIn bookingDtoIn, long userId) {
        User user = userService.findUser(userId);
        ItemDto item = itemService.findItem(bookingDtoIn.getItemId());
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет с Id " + item.getId() + " не доступен для бронирования");
        }
        Booking booking = bookingMapper.mapToBooking(bookingDtoIn);
        booking.setBooker(user);
        booking.setItem(itemMapper.mapToItem(item));
        booking.setStatus(BookingStatus.WAITING);
        bookingRepository.save(booking);
        return bookingMapper.mapToBookingDto(booking);
    }

    @Override
    public BookingDtoOut approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с Id " + bookingId + " не найдено"));

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
        User user = userService.findUser(userId);

        Booking foundBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с Id " + bookingId + " не найдено"));
        return bookingMapper.mapToBookingDto(foundBooking);
    }


}
