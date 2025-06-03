package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface ItemService {
    ItemDto findItem(Long id, Long userId);

    List<ItemDto> findUserItems(Long id, Integer from, Integer size);

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId);

    List<ItemDto> searchItems(String text);

    CommentDto addComment(Comment comment, Long itemId, Long userId);
}
