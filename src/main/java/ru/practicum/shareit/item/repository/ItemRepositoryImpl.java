package ru.practicum.shareit.item.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ItemRepositoryImpl implements ItemRepository {
    private final UserService us;
    private final Map<Long, List<Item>> userItems = new HashMap<>();
    long maxId = 1;

    @Override
    public Item findItem(Long id) {
        log.debug("Поиск вещи по id = {}", id);
        return userItems.values().stream()
                .flatMap(List::stream)
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Предмет с Id " + id + " не найден"));
    }

    @Override
    public List<Item> findUserItems(Long id) {
        log.debug("Поиск вещей пользователя с id = {}", id);
        return userItems.getOrDefault(id, Collections.emptyList());
    }

    @Override
    public Item addItem(Long userId, Item item) {
        log.debug("Добавление новой вещи: {}", item);
        checkUser(userId);
        item.setOwner(userId);
        item.setId(getMaxId());
        userItems.computeIfAbsent(userId, v -> new ArrayList<>()).add(item);
        return item;
    }

    @Override
    public Item updateItem(Long userId, Item item, Long itemId) {
        log.debug("Обновление вещи: {}", itemId);
        checkUser(userId);

        Item oldItem = findItem(itemId);
        if (!oldItem.getOwner().equals(userId)) {
            throw new NotFoundException("Обновляемая вещь с id = " + itemId + " не принадлежит " +
                    "указанному пользователю с id = " + userId);
        }
        if (item.getName() != null) {
            oldItem.setName(item.getName());
        }
        if (item.getDescription() != null) {
            oldItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            oldItem.setAvailable(item.getAvailable());
        }
        return oldItem;
    }

    @Override
    public List<Item> searchItems(String text) {
        log.debug("Поиск доступной вещи по названию или описанию {}", text);
        return userItems.values().stream()
                .flatMap(List::stream)
                .filter(item -> (item.getName().toUpperCase().equals(text)
                        || item.getDescription().toUpperCase().equals(text))
                        && item.getAvailable().equals(true))
                .toList();
    }

    private long getMaxId() {
        return ++maxId;
    }

    private void checkUser(Long id) {
        us.findUser(id);
    }
}
