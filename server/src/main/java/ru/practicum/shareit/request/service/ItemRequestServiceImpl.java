package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.shareit.user.repository.UserRepository;

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
    private final UserRepository userRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto create(ItemRequestDto dto, Long userId) {
        log.debug("Запрос на создание запроса вещи от пользователя {}", userId);
        User user = checkUser(userId);

        ItemRequest itemRequest = itemRequestMapper.mapToEntity(dto);

        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setOwner(user);

        itemRequest = itemRequestRepository.save(itemRequest);

        return itemRequestMapper.mapToItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.debug("Получен запрос на просмотр запросов вещей пользователя: {}", userId);
        checkUser(userId);
        List<ItemRequest> userRequests = itemRequestRepository.findByOwnerIdOrderByCreatedDesc(userId);
        return getRequestsItems(userRequests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.debug("Получен запрос на получение всех запросов вещей");
        checkUser(userId);
        Pageable page = PageRequest.of(from > 0 ? from / size : 0, size);
        List<ItemRequest> userRequests = itemRequestRepository.findByOwnerIdNotOrderByCreatedDesc(userId, page).getContent();
        return getRequestsItems(userRequests);
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.debug("Начинается получение запроса с id {} для пользователя {}", requestId, userId);
        checkUser(userId);

        ItemRequestDto itemRequestDto = itemRequestRepository.findById(requestId)
                .map(itemRequestMapper::mapToItemRequestDto)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        List<ItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(itemMapper::mapToItemDto).toList();

        itemRequestDto.setItems(items);

        log.debug("Запрос с id {} успешно получен, количество связанных предметов: {}", requestId, items.size());
        return itemRequestDto;
    }

    private List<ItemRequestDto> getRequestsItems(List<ItemRequest> requests) {
        log.debug("Начинается обработка списка запросов, количество: {}", requests.size());
        if (requests.isEmpty()) {
            log.debug("Список запросов пуст, возвращаем пустой результат");
            return Collections.emptyList();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<Item>> itemsGroup = items.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        List<ItemRequestDto> result = requests.stream()
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
        log.debug("Обработка запросов завершена, всего объектов возвращено: {}", result.size());
        return result;
    }

    private User checkUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}
