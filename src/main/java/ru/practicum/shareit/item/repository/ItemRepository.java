package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerId(Long id);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameText, String descriptionText);

}
