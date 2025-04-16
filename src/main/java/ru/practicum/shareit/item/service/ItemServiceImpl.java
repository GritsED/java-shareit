package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoOwner;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final BookingRepository bookingRepository;

    public ItemServiceImpl(ItemRepository itemRepository, ItemMapper itemMapper, UserService userService, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.itemMapper = itemMapper;
        this.userService = userService;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ItemDto findItem(Long id) {
        log.debug("Поиск вещи по id = {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет с Id " + id + " не найден"));
        return itemMapper.mapToItemDto(item);
    }

    @Override
    public List<ItemDtoOwner> findUserItems(Long id) {
        log.debug("Поиск вещей пользователя с id = {}", id);
        List<Item> items = itemRepository.findByOwnerId(id);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Booking> bookings = bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds);

        Map<Long, List<Booking>> bookingsGroup = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        return items.stream()
                .map(item -> {
                    List<Booking> bookingList = bookingsGroup.getOrDefault(item.getId(), Collections.emptyList());
                    LocalDateTime lastBooking = bookingList.stream()
                            .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                            .reduce((first, second) -> second)
                            .map(Booking::getEnd)
                            .orElse(null);

                    LocalDateTime nextBooking = bookingList.stream()
                            .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                            .findFirst()
                            .map(Booking::getEnd)
                            .orElse(null);

                    return itemMapper.mapToItemDtoOwner(item, lastBooking, nextBooking);
                }).toList();
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.debug("Добавление новой вещи: {}", itemDto);
        checkUser(userId);
        Item item = itemMapper.mapToItem(itemDto);
        item.setOwner(userService.findUser(userId));
        itemRepository.save(item);
        return itemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) {
        log.debug("Обновление вещи с Id: {}", itemId);
        checkUser(userId);

        Item oldItem = itemMapper.mapToItem(findItem(itemId));
        if (!oldItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Обновляемая вещь с id = " + itemId + " не принадлежит " +
                    "указанному пользователю с id = " + userId);
        }
        if (itemDto.getName() != null) {
            oldItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            oldItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            oldItem.setAvailable(itemDto.getAvailable());
        }
        Item savedItem = itemRepository.save(oldItem);
        return itemMapper.mapToItemDto(savedItem);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.debug("Поиск доступной вещи по названию или описанию {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        List<Item> foundItems = itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(text, text);
        return foundItems.stream().filter(Item::getAvailable).map(itemMapper::mapToItemDto).toList();
    }

    private void checkUser(Long id) {
        userService.findUser(id);
    }
}
