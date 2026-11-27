package com.loqma.customer.entity;

import com.loqma.common.enums.AddressLabel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_addresses_default", columnList = "customer_id, is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private AddressLabel label;

    @Column(length = 100)
    private String area;

    @Column(length = 150)
    private String street;

    @Column(length = 100)
    private String building;

    @Column(length = 20)
    private String floor;

    @Column(length = 20)
    private String apartment;

    @Column(length = 150)
    private String landmark;

    @Column(length = 20)
    private String phone;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Column(name = "gps_location", length = 100)
    private String gpsLocation;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
}