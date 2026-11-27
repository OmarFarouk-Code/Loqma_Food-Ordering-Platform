package com.loqma.order.entity;

import com.loqma.admin.entity.Admin;
import com.loqma.common.enums.OrderOption;
import com.loqma.common.enums.OrderStatus;
import com.loqma.customer.entity.Address;
import com.loqma.customer.entity.Customer;
import com.loqma.customer.entity.Review;
import com.loqma.driver.entity.Driver;
import com.loqma.owner.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_dispatch", columnList = "status, restaurant_id, driver_id"),
        @Index(name = "idx_orders_date", columnList = "order_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "order_number", length = 30, nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by_admin_id")
    private Admin cancelledByAdmin;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private OrderStatus status;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "ready_at")
    private LocalDateTime readyAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(nullable = false)
    private Double subtotal;

    @Column(name = "delivery_fee", nullable = false)
    private Double deliveryFee;

    @Column(name = "service_fee", nullable = false)
    @Builder.Default
    private Double serviceFee = 0.0;

    @Column(nullable = false)
    private Double tax;

    @Column(nullable = false)
    @Builder.Default
    private Double discount = 0.0;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "points_earned", nullable = false)
    @Builder.Default
    private Integer pointsEarned = 0;

    @Column(name = "points_redeemed", nullable = false)
    @Builder.Default
    private Integer pointsRedeemed = 0;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "option_flags", columnDefinition = "JSON")
    @Builder.Default
    private List<OrderOption> optionFlags = new ArrayList<>();

    @Column(name = "decline_or_cancellation_reason", length = 500)
    private String declineOrCancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Review review;
}