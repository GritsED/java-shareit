package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
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
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto findItem(Long id, Long userId) {
        log.debug("Поиск вещи по id = {}", id);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Предмет с Id " + id + " не найден"));
        List<Comment> comments = commentRepository.findAllByItemId(id);

        LocalDateTime lastBooking = null;
        LocalDateTime nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            lastBooking = bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(id,
                            LocalDateTime.now(),
                            BookingStatus.APPROVED)
                    .map(Booking::getEnd)
                    .orElse(null);

            nextBooking = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(id,
                            LocalDateTime.now())
                    .map(Booking::getStart)
                    .orElse(null);
        }
        return itemMapper.mapToItemDto(item, lastBooking, nextBooking, commentMapper.mapToCommentDto(comments));
    }

    @Override
    public List<ItemDto> findUserItems(Long id) {
        log.debug("Поиск вещей пользователя с id = {}", id);
        List<Item> items = itemRepository.findByOwnerId(id);
        List<Long> itemIds = items.stream().map(Item::getId).toList();

        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Booking> bookings = bookingRepository.findAllByItemIdInOrderByStartDesc(itemIds);

        Map<Long, List<Booking>> bookingsGroup = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        List<Comment> comments = commentRepository.findAllByItemIdIn(itemIds);

        Map<Long, List<Comment>> commentsGroup = comments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

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

                    List<Comment> comment = commentsGroup.getOrDefault(item.getId(), Collections.emptyList());

                    return itemMapper.mapToItemDto(item, lastBooking, nextBooking, commentMapper.mapToCommentDto(comment));
                }).toList();
    }

    @Override
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        log.debug("Добавление новой вещи: {}", itemDto);
        User user = checkUser(userId);
        Item item = itemMapper.mapToItem(itemDto);
        item.setOwner(user);
        itemRepository.save(item);
        return itemMapper.mapToItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, ItemDto itemDto, Long itemId) {
        log.debug("Обновление вещи с Id: {}", itemId);
        checkUser(userId);

        Item oldItem = itemMapper.mapToItem(findItem(itemId, userId));
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

    @Override
    public CommentDto addComment(Comment comment, Long itemId, Long userId) {
        User user = checkUser(userId);
        ItemDto item = findItem(itemId, userId);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now());
        if (bookings.isEmpty()) {
            throw new ValidationException("Пользователь " + userId + " не брал в аренду вещь " + itemId);
        }

        comment.setAuthor(user);
        comment.setItem(itemMapper.mapToItem(item));
        comment.setCreated(LocalDateTime.now());
        Comment saved = commentRepository.save(comment);
        return commentMapper.mapToCommentDto(saved);
    }

    private User checkUser(Long id) {
        return userService.findUser(id);
    }
}
