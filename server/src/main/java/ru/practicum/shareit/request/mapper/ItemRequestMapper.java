package ru.practicum.shareit.request.mapper;


import org.mapstruct.Mapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Mapper
public interface ItemRequestMapper {
    ItemRequestDto mapToItemRequestDto(ItemRequest itemRequest);

    ItemRequest mapToEntity(ItemRequestDto itemRequestDto);

    List<ItemRequestDto> mapToItemRequestDto(Iterable<ItemRequest> itemRequest);

}
