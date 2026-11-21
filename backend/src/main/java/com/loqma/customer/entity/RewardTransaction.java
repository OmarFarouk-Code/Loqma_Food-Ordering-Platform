package com.loqma.customer.entity;

import com.loqma.common.enums.TransactionType;
import com.loqma.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reward_transactions", indexes = {
        @Index(name = "idx_reward_tx_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private RewardAccount account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "order_number", length = 30)
    private String orderNumber;

    @Column(name = "restaurant_name", length = 100)
    private String restaurantName;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(name = "points_change", nullable = false)
    private Integer pointsChange;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private TransactionType type;
}