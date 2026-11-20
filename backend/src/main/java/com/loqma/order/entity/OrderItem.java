package com.loqma.order.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "item_name", length = 100, nullable = false)
    private String itemName;

    @Column(name = "selected_size", length = 50)
    private String selectedSize;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_extras", columnDefinition = "JSON")
    @Builder.Default
    private List<String> selectedExtras = new ArrayList<>();

    @Column(name = "customer_note", length = 500)
    private String customerNote;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_placement", nullable = false)
    private Double priceAtPlacement;
}