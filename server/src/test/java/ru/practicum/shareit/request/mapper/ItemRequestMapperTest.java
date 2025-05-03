package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ItemRequestMapperTest {

    private final ItemRequestMapperImpl mapper = new ItemRequestMapperImpl();

    @Test
    void mapToItemRequestDto_nullReturnsNull() {
        assertNull(mapper.mapToItemRequestDto((ItemRequest) null));
    }

    @Test
    void mapToEntity_nullReturnsNull() {
        assertNull(mapper.mapToEntity(null));
    }

    @Test
    void mapList_nullReturnsNull() {
        assertNull(mapper.mapToItemRequestDto((Iterable<ItemRequest>) null));
    }

    @Test
    void mapSimple() {
        ItemRequest ir = ItemRequest.builder()
                .id(30L)
                .description("Need X")
                .created(LocalDateTime.now())
                .items(List.of())
                .build();

        ItemRequestDto dto = mapper.mapToItemRequestDto(ir);
        assertEquals(30L, dto.getId());
        assertEquals("Need X", dto.getDescription());

        ItemRequest back = mapper.mapToEntity(ItemRequestDto.builder()
                .id(40L).description("Ask").created(LocalDateTime.now())
                .items(List.of(ItemDto.builder().id(50L).build()))
                .build());
        assertEquals(40L, back.getId());
        assertEquals(50L, back.getItems().get(0).getId());

        List<ItemRequestDto> list = mapper.mapToItemRequestDto(List.of(ir));
        assertEquals(1, list.size());
    }
}
