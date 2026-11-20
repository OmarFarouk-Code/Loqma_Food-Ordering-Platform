package com.loqma.owner.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_sizes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemSize {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @Column(name = "size_name", length = 50, nullable = false)
    private String sizeName;

    @Column(nullable = false)
    private Double price;
}