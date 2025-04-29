package ru.practicum.shareit.item.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CommentMapperTest {

    private final CommentMapperImpl mapper = new CommentMapperImpl();

    @Test
    void mapToCommentDto_nullReturnsNull() {
        assertNull(mapper.mapToCommentDto((Comment) null));
    }

    @Test
    void mapToComment_nullReturnsNull() {
        assertNull(mapper.mapToComment(null));
    }

    @Test
    void mapList_nullReturnsNull() {
        assertNull(mapper.mapToCommentDto((Iterable<Comment>) null));
    }

    @Test
    void mapSimple() {
        User u = User.builder().id(3L).name("Tom").build();
        Item i = Item.builder().id(8L).build();
        Comment c = Comment.builder()
                .id(5L).text("Nice").author(u).item(i).created(LocalDateTime.now())
                .build();

        CommentDto dto = mapper.mapToCommentDto(c);
        assertEquals("Tom", dto.getAuthorName());
        assertEquals(8L, dto.getItemId());

        Comment back = mapper.mapToComment(CommentDto.builder()
                .id(6L).text("Ok").authorName("Bob").itemId(9L).created(LocalDateTime.now())
                .build());
        assertEquals(6L, back.getId());
        assertEquals("Bob", back.getAuthor().getName());
        assertEquals(9L, back.getItem().getId());
    }

    @Test
    void mapIterable() {
        Comment c = Comment.builder().id(1L).build();
        List<CommentDto> dtos = mapper.mapToCommentDto(List.of(c));
        assertEquals(1, dtos.size());
    }
}

