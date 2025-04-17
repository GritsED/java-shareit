package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Mapper
public interface CommentMapper {
    @Mapping(source = "author.name", target = "authorName")
    @Mapping(source = "item.id", target = "itemId")
    CommentDto mapToCommentDto(Comment comment);

    @Mapping(source = "authorName", target = "author.name")
    @Mapping(source = "itemId", target = "item.id")
    Comment mapToComment(CommentDto commentDto);

    List<CommentDto> mapToCommentDto(Iterable<Comment> comments);
}
