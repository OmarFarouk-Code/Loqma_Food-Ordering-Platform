package com.loqma.owner.entity;

import com.loqma.customer.entity.Review;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_name_branch", columnList = "name, branch"),
        @Index(name = "idx_restaurants_cuisine", columnList = "cuisine")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private RestaurantOwner owner;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String branch;

    @Column(length = 100, nullable = false)
    private String cuisine;

    @Column(length = 500)
    private String logo;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(name = "delivery_fee", nullable = false)
    @Builder.Default
    private Double deliveryFee = 0.0;

    @Column(name = "minimum_order_amount", nullable = false)
    @Builder.Default
    private Double minimumOrderAmount = 0.0;

    @Column(name = "prep_time", nullable = false)
    @Builder.Default
    private Double prepTime = 25.0;

    @Column(name = "max_delivery_distance", nullable = false)
    @Builder.Default
    private Double maxDeliveryDistance = 15.0;

    @Column(name = "average_rating", nullable = false)
    @Builder.Default
    private Double averageRating = 5.0;

    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private Boolean isOpen = true;

    @Column(name = "gps_location", length = 100)
    private String gpsLocation;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OperatingHours> operatingHours = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant")
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();
}