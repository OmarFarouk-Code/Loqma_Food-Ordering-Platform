package com.loqma.customer.entity;

import com.loqma.customer.dto.ExtraDto;
import com.loqma.owner.entity.ItemExtra;
import com.loqma.owner.entity.ItemSize;
import com.loqma.owner.entity.MenuItem;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_size_id")
    private ItemSize selectedSize;

    @ManyToMany
    @JoinTable(
            name = "cart_item_extras",
            joinColumns = @JoinColumn(name = "cart_item_id"),
            inverseJoinColumns = @JoinColumn(name = "item_extra_id")
    )
    @Builder.Default
    private List<ItemExtra> selectedExtras = new ArrayList<>();

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "customer_note", length = 500)
    private String customerNote;

    @Column(name = "line_price", nullable = false)
    private Double linePrice;
}