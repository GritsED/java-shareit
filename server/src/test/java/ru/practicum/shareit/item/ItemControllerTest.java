package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private ItemDto itemDto2;
    private CommentDto comment;
    private List<ItemDto> items;

    @BeforeEach
    void setUp() {

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .build();
        comment = CommentDto.builder()
                .id(1L)
                .text("CommentText")
                .itemId(2L)
                .build();
        itemDto2 = ItemDto.builder()
                .id(2L)
                .name("Name2")
                .description("Description2")
                .available(true)
                .comments(List.of(comment))
                .build();


        items = List.of(itemDto, itemDto2);
    }

    @Test
    void getItem() throws Exception {
        when(itemService.findItem(anyLong(), anyLong())).thenReturn(itemDto);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.name").value("Name"),
                        jsonPath("$.description").value("Description"),
                        jsonPath("$.available").value(true)
                );
    }

    @Test
    void getItemsByUserid() throws Exception {
        when(itemService.findUserItems(anyLong(), any(), any())).thenReturn(items);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id").value(1L),
                        jsonPath("$[0].name").value("Name"),
                        jsonPath("$[0].description").value("Description"),
                        jsonPath("$[0].available").value(true)
                );
    }

    @Test
    void searchItems() throws Exception {
        when(itemService.searchItems("Name")).thenReturn(items);

        mvc.perform(get("/items/search?text=Name"))
                .andExpectAll(status().isOk(),
                        jsonPath("$[0].id").value(1L),
                        jsonPath("$[0].name").value("Name"),
                        jsonPath("$[0].description").value("Description"),
                        jsonPath("$[0].available").value(true)
                );
    }

    @Test
    void addItem() throws Exception {
        ItemDto itemToCreate = ItemDto.builder()
                .name("Name")
                .description("Description")
                .available(true)
                .build();

        when(itemService.addItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsBytes(itemToCreate))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.name").value("Name"),
                        jsonPath("$.description").value("Description"),
                        jsonPath("$.available").value(true)
                );
    }

    @Test
    void addComment() throws Exception {
        Comment comment1 = Comment.builder()
                .text("CommentText").build();
        when(itemService.addComment(any(Comment.class), anyLong(), anyLong())).thenReturn(comment);

        mvc.perform(post("/items/2/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsBytes(comment1))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.text").value("CommentText"),
                        jsonPath("$.itemId").value(2L));

    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(anyLong(), any(ItemDto.class), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsBytes(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpectAll(status().isOk(),
                        jsonPath("$.id").value(1L),
                        jsonPath("$.name").value("Name"),
                        jsonPath("$.description").value("Description"),
                        jsonPath("$.available").value(true));
    }
}