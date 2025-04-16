package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId) {
        return itemService.findItem(itemId);
    }

    @GetMapping
    public List<ItemDtoOwner> getItemsByUserid(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.findUserItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(required = false) final String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        } else {
            return itemService.searchItems(text);
        }
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @RequestBody @Valid ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId) {
        return itemService.updateItem(userId, itemDto, itemId);
    }
}
