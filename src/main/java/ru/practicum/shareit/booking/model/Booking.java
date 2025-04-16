package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.emun.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Builder
@Entity
@Table(name = "Bookings")
@AllArgsConstructor
@NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "start_date")
    LocalDateTime start;
    @Column(name = "end_date")
    LocalDateTime end;
    @ManyToOne
    @JoinColumn(name = "item_id")
    Item item;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User booker;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    BookingStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getBooker() {
        return booker;
    }

    public void setBooker(User booker) {
        this.booker = booker;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
