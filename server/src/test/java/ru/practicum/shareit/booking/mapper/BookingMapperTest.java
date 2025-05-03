package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class BookingMapperTest {

    private final BookingMapperImpl mapper = new BookingMapperImpl();

    @Test
    void mapToBookingDto_nullReturnsNull() {
        assertNull(mapper.mapToBookingDto((Booking) null));
    }

    @Test
    void mapToBooking_nullReturnsNull() {
        assertNull(mapper.mapToBooking(null));
    }

    @Test
    void mapList_nullReturnsNull() {
        assertNull(mapper.mapToBookingDto((Iterable<Booking>) null));
    }

    @Test
    void mapSimple() {
        User u = User.builder().id(4L).build();
        Item it = Item.builder().id(15L).name("Drill").description("d").available(true).build();
        Booking b = Booking.builder()
                .id(9L).booker(u).item(it)
                .start(LocalDateTime.now()).end(LocalDateTime.now()).status(BookingStatus.WAITING)
                .build();

        BookingDtoOut dto = mapper.mapToBookingDto(b);
        assertEquals(9L, dto.getId());
        assertEquals(4L, dto.getBooker().getId());
        assertEquals(15L, dto.getItem().getId());

        Booking back = mapper.mapToBooking(BookingDtoIn.builder()
                .itemId(20L).start(LocalDateTime.now()).end(LocalDateTime.now()).build());
        assertEquals(20L, back.getItem().getId());

        List<BookingDtoOut> list = mapper.mapToBookingDto(List.of(b));
        assertEquals(1, list.size());
    }
}
