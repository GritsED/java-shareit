package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
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
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@Import(ItemServiceImpl.class)
class ItemServiceImplTest {

    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private ItemMapper itemMapper;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BookingRepository bookingRepository;
    @MockBean
    private CommentRepository commentRepository;
    @MockBean
    private CommentMapper commentMapper;
    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private ItemService itemService;

    private User owner;
    private User booker;
    private Item item;
    private Booking lastBooking;
    private Booking nextBooking;
    private List<Comment> comments;
    private ItemDto expectedDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@mail.com");
        booker = new User(2L, "User", "user@mail.com");

        now = LocalDateTime.now();

        item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        lastBooking = Booking.builder()
                .id(100L)
                .start(now.minusDays(3))
                .end(now.minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        nextBooking = Booking.builder()
                .id(101L)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        comments = List.of();

        expectedDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(owner.getId())
                .requestId(null)
                .comments(List.of())
                .lastBooking(lastBooking.getEnd())
                .nextBooking(nextBooking.getStart())
                .build();
    }

    @Test
    void findItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(comments);
        when(bookingRepository.findFirstByItemIdAndEndBeforeAndStatusOrderByEndDesc(any(), any(), any(BookingStatus.class)))
                .thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(any(), any()))
                .thenReturn(Optional.of(nextBooking));
        when(commentMapper.mapToCommentDto(comments)).thenReturn(List.of());
        when(itemMapper.mapToItemDto(any(Item.class), any(), any(), anyList()))
                .thenReturn(expectedDto);

        ItemDto result = itemService.findItem(1L, 1L);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getName(), result.getName());
        assertEquals(expectedDto.getLastBooking(), result.getLastBooking());
        assertEquals(expectedDto.getNextBooking(), result.getNextBooking());
    }

    @Test
    void findItem_whenUserIsNotOwner_shouldReturnItemDtoWithoutBookings() {
        expectedDto.setLastBooking(null);
        expectedDto.setNextBooking(null);

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(commentRepository.findAllByItemId(1L)).thenReturn(comments);
        when(commentMapper.mapToCommentDto(comments)).thenReturn(List.of());

        when(itemMapper.mapToItemDto(any(), isNull(), isNull(), anyList()))
                .thenReturn(expectedDto);

        ItemDto result = itemService.findItem(1L, 999L);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }


    @Test
    void findItem_whenItemNotFound_shouldThrowNotFoundException() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.findItem(999L, 1L));

        assertEquals("Предмет с Id 999 не найден", exception.getMessage());
    }

    @Test
    void findUserItems_whenUserHasItem_shouldReturnDtoWithNullBookings() {
        List<Item> items = List.of(item);
        when(itemRepository.findByOwnerId(any())).thenReturn(items);
        when(bookingRepository.findAllByItemIdInOrderByStartDesc(anyList())).thenReturn(Collections.emptyList());
        when(commentRepository.findAllByItemIdIn(List.of(1L))).thenReturn(Collections.emptyList());
        when(commentMapper.mapToCommentDto(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(itemMapper.mapToItemDto(any(), isNull(), isNull(), anyList()))
                .thenReturn(expectedDto);

        List<ItemDto> result = itemService.findUserItems(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedDto, result.get(0));
    }

    @Test
    void findUserItems_whenUserHasNoItems_shouldReturnEmptyList() {
        when(itemRepository.findByOwnerId(any())).thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.findUserItems(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void addItem_whenRequestIdIsNull_shouldSaveItemWithoutRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.mapToItem(expectedDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.mapToItemDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.addItem(1L, expectedDto);

        assertNotNull(result);
        assertEquals(expectedDto, result);
        assertNull(item.getRequest());
    }

    @Test
    void addItem_whenRequestIdPresent_shouldSetRequestAndReturnDto() {
        ItemRequest itemRequest = ItemRequest.builder()
                .id(10L)
                .description("Нужен ноутбук")
                .owner(booker)
                .created(now.minusDays(2))
                .build();

        expectedDto.setRequestId(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(10L)).thenReturn(Optional.of(itemRequest));
        when(itemMapper.mapToItem(expectedDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.mapToItemDto(item)).thenReturn(expectedDto);

        ItemDto result = itemService.addItem(1L, expectedDto);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
        assertEquals(expectedDto.getRequestId(), result.getRequestId());
        assertEquals(expectedDto.getName(), result.getName());
    }


    @Test
    void updateItem_whenValidUserAndFieldsProvided_shouldUpdateItem() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Original Name")
                .description("Original Description")
                .available(true)
                .owner(owner)
                .build();

        ItemDto expectedDto = ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(owner.getId())
                .build();

        Item oldItem = Item.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(owner)
                .build();

        Item updatedItem = Item.builder()
                .id(item.getId())
                .name(updateDto.getName())
                .description(updateDto.getDescription())
                .available(updateDto.getAvailable())
                .owner(owner)
                .build();

        ItemDto expectedUpdatedDto = ItemDto.builder()
                .id(updatedItem.getId())
                .name(updatedItem.getName())
                .description(updatedItem.getDescription())
                .available(updatedItem.getAvailable())
                .ownerId(owner.getId())
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemMapper.mapToItemDto(any(Item.class), any(), any(), anyList())).thenReturn(expectedDto);
        when(itemMapper.mapToItem(any(ItemDto.class))).thenReturn(oldItem);
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.mapToItemDto(any(Item.class))).thenReturn(expectedUpdatedDto);

        ItemDto result = itemService.updateItem(owner.getId(), updateDto, item.getId());

        assertNotNull(result);
        assertEquals(expectedUpdatedDto, result);
    }

    @Test
    void updateItem_whenUserDoesNotOwnItem_shouldThrowNotFoundException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        Item oldItem = Item.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .owner(owner)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemMapper.mapToItemDto(any(Item.class), any(), any(), any())).thenReturn(expectedDto);
        when(itemMapper.mapToItem(any(ItemDto.class))).thenReturn(oldItem);

        NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            itemService.updateItem(999L, updateDto, item.getId());
        });

        assertEquals("Обновляемая вещь с id = " + item.getId() + " не принадлежит " +
                "указанному пользователю с id = 999", thrown.getMessage());
    }


    @Test
    void searchItems_whenNoItemsFound_shouldReturnEmptyList() {
        String searchText = "NonExistingText";

        when(itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        List<ItemDto> result = itemService.searchItems(searchText);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Результат поиска должен быть пустым");
    }

    @Test
    void searchItems_whenItemsFoundAndAvailable_shouldReturnItemDtos() {
        String searchText = "item";

        Item availableItem = Item.builder()
                .id(1L)
                .name("Available Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemDto availableItemDto = ItemDto.builder()
                .id(availableItem.getId())
                .name(availableItem.getName())
                .description(availableItem.getDescription())
                .available(availableItem.getAvailable())
                .ownerId(owner.getId())
                .build();

        when(itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(availableItem));

        when(itemMapper.mapToItemDto(availableItem)).thenReturn(availableItemDto);

        List<ItemDto> result = itemService.searchItems(searchText);

        assertNotNull(result);
        assertEquals(1, result.size(), "Ожидаем один результат");
        assertEquals(availableItemDto, result.get(0), "Ожидаем правильный ItemDto");
    }

    @Test
    void searchItems_whenItemsFoundButNotAvailable_shouldReturnEmptyList() {
        String searchText = "item";

        Item unavailableItem = Item.builder()
                .id(2L)
                .name("Unavailable Item")
                .description("Description")
                .available(false)
                .owner(owner)
                .build();

        when(itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(anyString(), anyString()))
                .thenReturn(List.of(unavailableItem));

        List<ItemDto> result = itemService.searchItems(searchText);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Предметы, которые не доступны, не должны быть возвращены");
    }

    @Test
    void searchItems_whenSearchTextIsEmpty_shouldReturnEmptyList() {
        String searchText = "";

        List<ItemDto> result = itemService.searchItems(searchText);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Поиск по пустому тексту должен вернуть пустой список");
    }

    @Test
    void addComment_whenUserDidNotRentItem_shouldThrowValidationException() {
        Long itemId = 1L;
        Long userId = 1L;
        Comment comment = null;

        when(bookingRepository.findAllByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now()))
                .thenReturn(Collections.emptyList());
        when(userRepository.findById(any())).thenReturn(Optional.of(owner));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemMapper.mapToItemDto(any(Item.class), any(), any(), any())).thenReturn(expectedDto);

        assertThrows(ValidationException.class, () -> itemService.addComment(comment, itemId, userId),
                "Ожидаем исключение ValidationException, потому что пользователь не брал вещь в аренду");
    }

    @Test
    void addComment_shouldReturnCommentDto_whenUserRentedItem() {
        Comment comment = Comment.builder().text("Отличная вещь").build();

        CommentDto commentDto = CommentDto.builder().text("Отличная вещь").build();

        List<Booking> bookings = List.of(Booking.builder().booker(booker).item(item).build());

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemMapper.mapToItemDto(any(Item.class), any(), any(), any())).thenReturn(expectedDto);
        when(bookingRepository.findAllByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(bookings);
        when(commentRepository.save(any())).thenReturn(comment);
        when(commentMapper.mapToCommentDto(comment)).thenReturn(commentDto);

        CommentDto result = itemService.addComment(comment, item.getId(), booker.getId());

        assertNotNull(result);
        assertEquals("Отличная вещь", result.getText());
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenUserNotFound() {
        Comment comment = Comment.builder().text("Отличная вещь").build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(comment, item.getId(), booker.getId()));
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenItemNotFound() {
        Comment comment = Comment.builder().text("Отличная вещь").build();

        when(userRepository.findById(booker.getId())).thenReturn(Optional.of(booker));
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(comment, item.getId(), booker.getId()));
    }


}