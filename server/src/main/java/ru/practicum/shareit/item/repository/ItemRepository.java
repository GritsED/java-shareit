package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByOwnerId(Long id, Pageable page);

    List<Item> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameText, String descriptionText);

    List<Item> findAllByRequestIdIn(Iterable<Long> ids);

    List<Item> findAllByRequestId(Long requestId);
}
