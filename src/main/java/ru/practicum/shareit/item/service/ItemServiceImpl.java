package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository ir;

    @Override
    public ItemDto findItem(Long id) {
        Item item = ir.findItem(id);
        return ItemMapper.MapToItemDto(item);
    }

    @Override
    public List<Item> findUserItems(Long id) {
        return ir.findUserItems(id);
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        Item item = ItemMapper.MapToItem(itemDto);
        Item item1 = ir.addItem(userId, item);
        return ItemMapper.MapToItemDto(item1);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) {
        Item item = ItemMapper.MapToItem(itemDto);
        return ItemMapper.MapToItemDto(ir.updateItem(userId, item, itemId));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return ir.searchItems(text).stream()
                .map(ItemMapper::MapToItemDto)
                .toList();
    }
}
