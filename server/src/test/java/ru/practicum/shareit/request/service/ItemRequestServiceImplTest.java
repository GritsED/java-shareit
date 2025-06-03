package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringJUnitConfig
@Import(ItemRequestServiceImpl.class)
class ItemRequestServiceImplTest {

    Pageable pageable;
    int from = 0;
    int size = 10;
    @MockBean
    private ItemRequestMapper itemRequestMapper;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private ItemRequestRepository itemRequestRepository;
    @MockBean
    private ItemRepository itemRepository;
    @MockBean
    private ItemMapper itemMapper;
    @Autowired
    private ItemRequestServiceImpl itemRequestService;
    private User user;
    private ItemRequestDto requestDto;
    private ItemRequest request;
    private ItemRequestDto mappedDto;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("User").email("u@mail.com").build();
        pageable = PageRequest.of(from / size, size);

        requestDto = ItemRequestDto.builder()
                .description("Need item")
                .build();

        request = ItemRequest.builder()
                .id(5L)
                .description("Need item")
                .created(LocalDateTime.now())
                .owner(user)
                .build();

        mappedDto = ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(Collections.emptyList())
                .build();

        item = Item.builder()
                .id(10L)
                .request(request)
                .build();

        itemDto = ItemDto.builder()
                .id(item.getId())
                .requestId(request.getId())
                .build();
    }

    @Test
    void create_shouldMapAndReturnDto() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestMapper.mapToEntity(requestDto)).thenReturn(request);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(itemRequestMapper.mapToItemRequestDto(request)).thenReturn(mappedDto);

        ItemRequestDto result = itemRequestService.create(requestDto, user.getId());

        assertNotNull(result);
        assertEquals(mappedDto, result);
    }

    @Test
    void create_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(requestDto, 99L));
    }

    @Test
    void getUserRequests_shouldReturnEmptyList_whenNoRequests() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByOwnerIdOrderByCreatedDesc(user.getId()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> result = itemRequestService.getUserRequests(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void getUserRequests_shouldReturnMappedList() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByOwnerIdOrderByCreatedDesc(user.getId()))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequestIdIn(List.of(request.getId())))
                .thenReturn(List.of(item));
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);
        when(itemRequestMapper.mapToItemRequestDto(request)).thenReturn(mappedDto);

        List<ItemRequestDto> result = itemRequestService.getUserRequests(user.getId());

        assertEquals(1, result.size());
        assertEquals(itemDto, result.get(0).getItems().get(0));
    }

    @Test
    void getAllRequests_shouldReturnEmptyList_whenNoOtherRequests() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByOwnerIdNotOrderByCreatedDesc(user.getId(), pageable))
                .thenReturn(Page.empty());

        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), from, size);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllRequests_shouldReturnMappedList() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByOwnerIdNotOrderByCreatedDesc(user.getId(), pageable))
                .thenReturn(new PageImpl<>(List.of(request)));
        when(itemRepository.findAllByRequestIdIn(List.of(request.getId())))
                .thenReturn(List.of(item));
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);
        when(itemRequestMapper.mapToItemRequestDto(request)).thenReturn(mappedDto);

        List<ItemRequestDto> result = itemRequestService.getAllRequests(user.getId(), from, size);

        assertEquals(1, result.size());
        assertEquals(itemDto, result.get(0).getItems().get(0));
    }

    @Test
    void getRequestById_shouldReturnDtoWithItems() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(request.getId()))
                .thenReturn(Optional.of(request));
        when(itemRequestMapper.mapToItemRequestDto(request)).thenReturn(mappedDto);
        when(itemRepository.findAllByRequestId(request.getId()))
                .thenReturn(List.of(item));
        when(itemMapper.mapToItemDto(item)).thenReturn(itemDto);

        ItemRequestDto result = itemRequestService.getRequestById(user.getId(), request.getId());

        assertNotNull(result);
        assertEquals(mappedDto.getId(), result.getId());
        assertEquals(itemDto, result.getItems().get(0));
    }

    @Test
    void getRequestById_shouldThrowNotFound_whenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(99L, request.getId()));
    }

    @Test
    void getRequestById_shouldThrowNotFound_whenRequestNotFound() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(user.getId(), 999L));
    }
}