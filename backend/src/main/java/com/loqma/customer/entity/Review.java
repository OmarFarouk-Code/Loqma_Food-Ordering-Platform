package com.loqma.customer.entity;

import com.loqma.driver.entity.Driver;
import com.loqma.order.entity.Order;
import com.loqma.owner.entity.Restaurant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "customer_name", length = 100, nullable = false)
    private String customerName;

    @Column(name = "restaurant_rating", nullable = false)
    private Integer restaurantRating;

    @Column(name = "driver_rating")
    private Integer driverRating;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "restaurant_tags", columnDefinition = "JSON")
    @Builder.Default
    private List<String> restaurantTags = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "driver_tags", columnDefinition = "JSON")
    @Builder.Default
    private List<String> driverTags = new ArrayList<>();

    @Column(name = "restaurant_comment", columnDefinition = "TEXT")
    private String restaurantComment;

    @Column(name = "driver_comment", columnDefinition = "TEXT")
    private String driverComment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}