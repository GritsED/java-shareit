package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoOwner {
    Long id;
    String name;
    String description;
    Boolean available;
    Long ownerId;
    LocalDateTime lastBooking;
    LocalDateTime nextBooking;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getLastBooking() {
        return lastBooking;
    }

    public void setLastBooking(LocalDateTime lastBooking) {
        this.lastBooking = lastBooking;
    }

    public LocalDateTime getNextBooking() {
        return nextBooking;
    }

    public void setNextBooking(LocalDateTime nextBooking) {
        this.nextBooking = nextBooking;
    }
}
