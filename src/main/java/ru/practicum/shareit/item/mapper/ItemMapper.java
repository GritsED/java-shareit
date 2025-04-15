package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ItemMapper {
    @Mapping(source = "ownerId", target = "owner.id")
    Item mapToItem(ItemDto itemDto);

    @Mapping(source = "owner.id", target = "ownerId")
    ItemDto mapToItemDto(Item item);

    List<ItemDto> mapToItemDto(Iterable<Item> items);

//    public static Item mapToItem(ItemDto item) {
//        return Item.builder()
//                .name(item.getName())
//                .description(item.getDescription())
//                .available(item.getAvailable())
//                .build();
//    }
//
//    public static ItemDto mapToItemDto(Item item) {
//        return ItemDto.builder()
//                .id(item.getId())
//                .name(item.getName())
//                .description(item.getDescription())
//                .available(item.getAvailable())
//                .build();
//    }
}
