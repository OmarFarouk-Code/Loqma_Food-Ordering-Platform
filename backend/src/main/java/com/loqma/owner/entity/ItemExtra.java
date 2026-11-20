package com.loqma.owner.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_extras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemExtra {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;
}