package com.loqma.customer.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "card_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "card_number", length = 25, nullable = false)
    private String cardNumber;

    @Column(name = "expiry_date", length = 10, nullable = false)
    private String expiryDate;

    @Column(length = 4, nullable = false)
    private String cvv;

    @Column(name = "cardholder_name", length = 100, nullable = false)
    private String cardholderName;
}