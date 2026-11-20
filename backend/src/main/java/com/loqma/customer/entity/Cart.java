package com.loqma.customer.entity;

import com.loqma.order.entity.Coupon;
import com.loqma.owner.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon appliedCoupon;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Double subtotal = 0.0;

    @Column(name = "delivery_fee", nullable = false)
    @Builder.Default
    private Double deliveryFee = 0.0;

    @Column(name = "service_fee", nullable = false)
    @Builder.Default
    private Double serviceFee = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double tax = 0.0;

    @Column(nullable = false)
    @Builder.Default
    private Double discount = 0.0;

    @Column(name = "total_price", nullable = false)
    @Builder.Default
    private Double totalPrice = 0.0;

    @Column(name = "minimum_order_amount", nullable = false)
    @Builder.Default
    private Double minimumOrderAmount = 0.0;
}