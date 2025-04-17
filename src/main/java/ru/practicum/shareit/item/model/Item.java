package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.user.model.User;

@Builder
@Getter
@Setter
@Entity
@Table(name = "Items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "name")
    @NotBlank(message = "Имя не может быть пустым")
    String name;
    @Column(name = "description")
    @NotBlank(message = "Описание не может быть пустым")
    String description;
    @Column(name = "available")
    @NotNull(message = "Поле \"available\" не может быть пустым")
    Boolean available;
    @ManyToOne
    @JoinColumn(name = "user_id")
    User owner;
}
