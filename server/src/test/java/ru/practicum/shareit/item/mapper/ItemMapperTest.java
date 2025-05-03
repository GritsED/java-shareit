package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    private final ItemMapperImpl mapper = new ItemMapperImpl();

    @Test
    void mapToItem_nullReturnsNull() {
        assertNull(mapper.mapToItem(null));
    }

    @Test
    void mapToItemDto_nullReturnsNull() {
        assertNull(mapper.mapToItemDto(null));
        assertNull(mapper.mapToItemDto(null, null, null, null));
    }

    @Test
    void mapSimple() {
        ItemDto dto = ItemDto.builder()
                .id(10L)
                .name("Drill")
                .description("desc")
                .available(true)
                .ownerId(5L)
                .build();

        Item item = mapper.mapToItem(dto);
        assertNotNull(item);
        assertEquals(10L, item.getId());
        assertEquals(5L, item.getOwner().getId());

        ItemDto dto2 = mapper.mapToItemDto(item);
        assertEquals(item.getId(), dto2.getId());
    }

    @Test
    void mapWithExtras() {
        User owner = User.builder().id(5L).build();
        Item item = Item.builder()
                .id(20L).owner(owner)
                .name("Saw").description("d").available(false)
                .build();

        LocalDateTime last = LocalDateTime.now().minusDays(1);
        LocalDateTime next = LocalDateTime.now().plusDays(1);
        CommentDto c = CommentDto.builder().id(7L).text("ok").authorName("u").itemId(20L).build();
        ItemDto dto = mapper.mapToItemDto(item, last, next, List.of(c));

        assertNotNull(dto);
        assertEquals(last, dto.getLastBooking());
        assertEquals(next, dto.getNextBooking());
        assertEquals(1, dto.getComments().size());
    }
}
