package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.emun.BookingState;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@Import(BookingServiceImpl.class)
class BookingServiceImplTest {
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private BookingMapper bookingMapper;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRepository itemRepository;
    @Autowired
    private BookingService bookingService;

    private BookingDtoIn bookingDtoIn;
    private Booking booking;
    private BookingDtoOut bookingDtoOut;
    private User user;
    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        Long userId = 1L;
        Long itemId = 2L;
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);

        user = User.builder()
                .id(userId)
                .name("User")
                .email("user@mail.com")
                .build();

        owner = User.builder()
                .id(99L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        item = Item.builder()
                .id(itemId)
                .name("Item")
                .description("Test item")
                .available(true)
                .owner(owner)
                .build();

        itemDto = ItemDto.builder()
                .id(itemId)
                .name("Item")
                .description("Test item")
                .available(true)
                .ownerId(owner.getId())
                .build();

        bookingDtoIn = BookingDtoIn.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        booking = Booking.builder()
                .id(10L)
                .start(start)
                .end(end)
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();

        bookingDtoOut = BookingDtoOut.builder()
                .id(10L)
                .start(start)
                .end(end)
                .item(itemDto)
                .booker(user)
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_shouldReturnBookingDto_whenInputIsValid() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(bookingMapper.mapToBooking(bookingDtoIn)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDtoOut);

        BookingDtoOut result = bookingService.createBooking(bookingDtoIn, user.getId());

        assertNotNull(result);
        assertEquals(bookingDtoOut.getId(), result.getId());
        assertEquals(bookingDtoOut.getStatus(), result.getStatus());
        assertEquals(bookingDtoOut.getBooker().getId(), result.getBooker().getId());

        verify(userRepository).findById(user.getId());
        verify(itemRepository).findById(item.getId());
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingMapper).mapToBookingDto(booking);
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoIn, user.getId()));

        assertEquals("Пользователь с id 1 не найден", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenItemNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(bookingDtoIn, user.getId()));

        assertEquals("Предмет с Id 2 не найден", ex.getMessage());
    }

    @Test
    void createBooking_shouldThrowException_whenItemNotAvailable() {
        item.setAvailable(false);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(bookingDtoIn, user.getId()));

        assertEquals("Предмет с Id 2 не доступен для бронирования", ex.getMessage());
    }

    @Test
    void approve_shouldApproveBooking_whenApprovedTrueAndStatusWaitingAndUserIsOwner() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDtoOut);

        BookingDtoOut result = bookingService.approve(owner.getId(), booking.getId(), true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void approve_shouldRejectBooking_whenApprovedFalseAndStatusWaitingAndUserIsOwner() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDtoOut);

        BookingDtoOut result = bookingService.approve(owner.getId(), booking.getId(), false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void approve_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.approve(owner.getId(), booking.getId(), true));

        assertTrue(ex.getMessage().contains("не найден"));
    }

    @Test
    void approve_shouldThrowValidationException_whenUserIsNotOwner() {
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.approve(user.getId(), booking.getId(), true));

        assertTrue(ex.getMessage().contains("не является владельцем"));
    }

    @Test
    void approve_shouldThrowValidationException_whenStatusNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.approve(owner.getId(), booking.getId(), true));

        assertEquals("Бронирование подтверждено или отклонено", ex.getMessage());
    }

    @Test
    void findById_shouldReturnBookingDto_whenUserExistsAndBookingExists() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingMapper.mapToBookingDto(booking)).thenReturn(bookingDtoOut);

        BookingDtoOut result = bookingService.findById(booking.getId(), user.getId());

        assertNotNull(result);
        assertEquals(bookingDtoOut.getId(), result.getId());
        verify(bookingMapper).mapToBookingDto(booking);
    }

    @Test
    void findById_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.findById(booking.getId(), user.getId()));

        assertTrue(ex.getMessage().contains("Пользователь с id"));
    }

    @Test
    void findById_shouldThrowNotFoundException_whenBookingNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> bookingService.findById(booking.getId(), user.getId()));

        assertTrue(ex.getMessage().contains("Бронирование с Id"));
    }

    @Test
    void findAll_shouldReturnList_forDefaultState() {
        List<Booking> bookings = Collections.singletonList(booking);
        List<BookingDtoOut> dtoList = Collections.singletonList(bookingDtoOut);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerId(user.getId())).thenReturn(bookings);
        when(bookingMapper.mapToBookingDto(bookings)).thenReturn(dtoList);

        List<BookingDtoOut> result = bookingService.findAll(user.getId(), "ALL");

        assertEquals(1, result.size());
        assertEquals(bookingDtoOut, result.get(0));
    }

    @Test
    void findAll_shouldReturnList_forCurrentState() {
        List<Booking> bookings = Collections.singletonList(booking);
        List<BookingDtoOut> dtoList = Collections.singletonList(bookingDtoOut);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(bookingRepository.findAllByBookerIdAndEndAfterOrderByStartDesc(anyLong(), any(LocalDateTime.class)))
                .thenReturn(bookings);
        when(bookingMapper.mapToBookingDto(bookings)).thenReturn(dtoList);

        List<BookingDtoOut> result = bookingService.findAll(user.getId(), "CURRENT");

        assertEquals(1, result.size());
        assertEquals(bookingDtoOut, result.get(0));
    }

    @Test
    void findAllByItemOwner_shouldReturnList_forDefaultState() {
        List<Booking> bookings = Collections.singletonList(booking);
        List<BookingDtoOut> dtoList = Collections.singletonList(bookingDtoOut);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(owner.getId())).thenReturn(bookings);
        when(bookingMapper.mapToBookingDto(bookings)).thenReturn(dtoList);

        List<BookingDtoOut> result = bookingService.findAllByItemOwner(owner.getId(), "ALL");

        assertEquals(1, result.size());
        assertEquals(bookingDtoOut, result.get(0));
    }

    @Test
    void findAllByItemOwner_shouldReturnList_forWaitingState() {
        List<Booking> bookings = Collections.singletonList(booking);
        List<BookingDtoOut> dtoList = Collections.singletonList(bookingDtoOut);

        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(owner.getId(), BookingState.WAITING))
                .thenReturn(bookings);
        when(bookingMapper.mapToBookingDto(bookings)).thenReturn(dtoList);

        List<BookingDtoOut> result = bookingService.findAllByItemOwner(owner.getId(), "WAITING");

        assertEquals(1, result.size());
        assertEquals(bookingDtoOut, result.get(0));
    }

    @Test
    void findAllByItemOwner_shouldThrowNotFoundException_whenNoBookings() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(owner.getId())).thenReturn(Collections.emptyList());

        assertThrows(NotFoundException.class,
                () -> bookingService.findAllByItemOwner(owner.getId(), "ALL"));
    }
}