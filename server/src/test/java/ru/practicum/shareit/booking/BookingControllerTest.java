package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
import ru.practicum.shareit.booking.dto.BookingDtoOut;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    BookingDtoIn bookingDtoIn;
    BookingDtoOut bookingDtoOut;
    List<BookingDtoOut> bookingDtoOutList;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        bookingDtoIn = BookingDtoIn.builder()
                .itemId(1L)
                .start(LocalDateTime.now().withNano(0))
                .end(LocalDateTime.now().plusDays(1).withNano(0))
                .build();

        bookingDtoOut = BookingDtoOut.builder()
                .id(1L)
                .start(bookingDtoIn.getStart())
                .end(bookingDtoIn.getEnd())
                .item(ItemDto.builder().id(1L).build())
                .booker(User.builder().id(1L).build())
                .status(BookingStatus.WAITING).build();

        bookingDtoOutList = List.of(bookingDtoOut);
    }

    @Test
    void createBooking() throws Exception {
        when(bookingService.createBooking(any(BookingDtoIn.class), anyLong()))
                .thenReturn(bookingDtoOut);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsBytes(bookingDtoIn))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.start").value(bookingDtoIn.getStart().toString()),
                        jsonPath("$.end").value(bookingDtoIn.getEnd().toString()),
                        jsonPath("$.item.id").value(bookingDtoIn.getItemId()),
                        jsonPath("$.booker.id").value(bookingDtoOut.getBooker().getId()),
                        jsonPath("$.status").value(bookingDtoOut.getStatus().toString())
                );

    }

    @Test
    void approve() throws Exception {
        BookingDtoOut dtoOutApprove = BookingDtoOut.builder()
                .id(1L)
                .start(bookingDtoIn.getStart())
                .end(bookingDtoIn.getEnd())
                .item(ItemDto.builder().id(1L).build())
                .booker(User.builder().id(1L).build())
                .status(BookingStatus.APPROVED).build();

        when(bookingService.approve(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(dtoOutApprove);

        mvc.perform(patch("/bookings/1")
                        .queryParam("approved", "true")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.start").value(dtoOutApprove.getStart().toString()),
                        jsonPath("$.end").value(dtoOutApprove.getEnd().toString()),
                        jsonPath("$.item.id").value(bookingDtoIn.getItemId()),
                        jsonPath("$.booker.id").value(dtoOutApprove.getBooker().getId()),
                        jsonPath("$.status").value(dtoOutApprove.getStatus().toString()));
    }

    @Test
    void getBookingByUserId() throws Exception {
        when(bookingService.findById(anyLong(), anyLong())).thenReturn(bookingDtoOut);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.start").value(bookingDtoIn.getStart().toString()),
                        jsonPath("$.end").value(bookingDtoIn.getEnd().toString()),
                        jsonPath("$.item.id").value(bookingDtoIn.getItemId()),
                        jsonPath("$.booker.id").value(bookingDtoOut.getBooker().getId()),
                        jsonPath("$.status").value(bookingDtoOut.getStatus().toString())
                );
    }

    @Test
    void getAllBookings() throws Exception {
        when(bookingService.findAll(anyLong(), anyString())).thenReturn(bookingDtoOutList);

        mvc.perform(get("/bookings")
                        .queryParam("state", "ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id").value(1L),
                        jsonPath("$[0].start").value(bookingDtoOut.getStart().toString()),
                        jsonPath("$[0].end").value(bookingDtoOut.getEnd().toString()),
                        jsonPath("$[0].item.id").value(bookingDtoIn.getItemId()),
                        jsonPath("$[0].booker.id").value(bookingDtoOut.getBooker().getId()),
                        jsonPath("$[0].status").value(bookingDtoOut.getStatus().toString()));
    }

    @Test
    void getAllBookingByItemOwner() throws Exception {
        when(bookingService.findAllByItemOwner(anyLong(), anyString())).thenReturn(bookingDtoOutList);

        mvc.perform(get("/bookings/owner")
                        .queryParam("state", "ALL")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id").value(1L),
                        jsonPath("$[0].start").value(bookingDtoOut.getStart().toString()),
                        jsonPath("$[0].end").value(bookingDtoOut.getEnd().toString()),
                        jsonPath("$[0].item.id").value(bookingDtoIn.getItemId()),
                        jsonPath("$[0].booker.id").value(bookingDtoOut.getBooker().getId()),
                        jsonPath("$[0].status").value(bookingDtoOut.getStatus().toString()));
    }
}