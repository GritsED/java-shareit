package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestMapper itemRequestMapper;
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        log.debug("Запрос на создание запроса вещи от пользователя {}", userId);
        User user = userService.findUser(userId);

        ItemRequest itemRequest = itemRequestMapper.mapToEntity(dto);

        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setOwner(user);

        itemRequest = itemRequestRepository.save(itemRequest);

        return itemRequestMapper.mapToItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.debug("Получен запрос на просмотр запросов вещей пользователя: {}", userId);
        userService.findUser(userId);
        List<ItemRequest> userRequests = itemRequestRepository.findByOwnerIdOrderByCreatedDesc(userId);
        return getRequestsItems(userRequests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.debug("Получен запрос на получение всех запросов вещей");
        userService.findUser(userId);
        List<ItemRequest> userRequests = itemRequestRepository.findByOwnerIdNotOrderByCreatedDesc(userId);
        return getRequestsItems(userRequests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        userService.findUser(userId);

        ItemRequestDto itemRequestDto = itemRequestRepository.findById(requestId)
                .map(itemRequestMapper::mapToItemRequestDto)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<ItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(itemMapper::mapToItemDto).toList();

        itemRequestDto.setItems(items);

        return itemRequestDto;
    }

    private List<ItemRequestDto> getRequestsItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsGroup = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(itemRequest -> {
                    List<Item> itemsList = itemsGroup.getOrDefault(itemRequest.getId(), Collections.emptyList());

                    List<ItemDto> itemDtoList = itemsList.stream()
                            .map(itemMapper::mapToItemDto)
                            .toList();

                    ItemRequestDto itemRequestDto = itemRequestMapper.mapToItemRequestDto(itemRequest);
                    itemRequestDto.setItems(itemDtoList);
                    return itemRequestDto;
                })
                .toList();
    }
}
