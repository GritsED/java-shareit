package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;

import java.util.List;

public interface ItemService {
    ItemDto findItem(Long id);

    List<ItemDtoOwner> findUserItems(Long id);

    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId);

    List<ItemDto> searchItems(String text);
}
