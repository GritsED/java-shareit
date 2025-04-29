package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@AllArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    ItemRequestDto create(@RequestBody ItemRequestDto dto,
                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.create(dto, userId);
    }

    @GetMapping
    List<ItemRequestDto> getRequestByOwner(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getUserRequests(userId);
    }

    @GetMapping("/all")
    List<ItemRequestDto> getRequests(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    ItemRequestDto getRequestById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}
