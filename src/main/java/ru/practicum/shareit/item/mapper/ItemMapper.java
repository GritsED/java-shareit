package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {
    @Mapping(source = "ownerId", target = "owner.id")
    Item mapToItem(ItemDto itemDto);

    @Mapping(source = "owner.id", target = "ownerId")
    ItemDto mapToItemDto(Item item);

    @Mapping(source = "item.owner.id", target = "ownerId")
    @Mapping(source = "lastBooking", target = "lastBooking")
    @Mapping(source = "nextBooking", target = "nextBooking")
    @Mapping(source = "comments", target = "comments")
    ItemDto mapToItemDto(Item item, LocalDateTime lastBooking, LocalDateTime nextBooking, List<CommentDto> comments);
}
