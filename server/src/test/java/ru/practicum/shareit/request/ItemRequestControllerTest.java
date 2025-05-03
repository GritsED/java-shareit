package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {
    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mvc;

    private ItemRequestDto itemRequestDto;
    private ItemRequestDto itemRequestDto2;
    private List<ItemRequestDto> itemRequestDtos;

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .description("Description")
                .created(LocalDateTime.now().withNano(0))
                .build();

        itemRequestDto2 = ItemRequestDto.builder()
                .description("Description2")
                .created(LocalDateTime.now().withNano(0))
                .build();

        itemRequestDtos = List.of(itemRequestDto);
    }

    @Test
    void create() throws Exception {
        when(itemRequestService.create(any(ItemRequestDto.class), anyLong())).thenReturn(itemRequestDto2);

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsBytes(itemRequestDto2))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.description").value(itemRequestDto2.getDescription()),
                        jsonPath("$.created").value(itemRequestDto2.getCreated().toString()));
    }

    @Test
    void getRequestByOwner() throws Exception {
        when(itemRequestService.getUserRequests(anyLong())).thenReturn(itemRequestDtos);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].description").value(itemRequestDto.getDescription()),
                        jsonPath("$[0].created").value(itemRequestDto.getCreated().toString()));
    }

    @Test
    void getRequests() throws Exception {
        when(itemRequestService.getAllRequests(anyLong(), any(), any())).thenReturn(itemRequestDtos);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].description").value(itemRequestDto.getDescription()),
                        jsonPath("$[0].created").value(itemRequestDto.getCreated().toString()));
    }

    @Test
    void getRequestById() throws Exception {
        when(itemRequestService.getRequestById(anyLong(), anyLong())).thenReturn(itemRequestDto);

        mvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$.description").value(itemRequestDto.getDescription()),
                        jsonPath("$.created").value(itemRequestDto.getCreated().toString()));
    }
}