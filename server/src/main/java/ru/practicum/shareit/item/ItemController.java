package ru.practicum.shareit.item;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto getItem(@PathVariable Long itemId,
                           @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.findItem(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItemsByUserid(@RequestHeader("X-Sharer-User-Id") Long userId,
                                          @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                          @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return itemService.findUserItems(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam(required = false) final String text) {
        return itemService.searchItems(text);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @RequestBody ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody Comment comment,
                                 @PathVariable Long itemId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.addComment(comment, itemId, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto,
                              @PathVariable Long itemId) {
        return itemService.updateItem(userId, itemDto, itemId);
    }
}
