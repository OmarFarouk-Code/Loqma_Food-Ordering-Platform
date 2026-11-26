package com.loqma.driver.entity;

import com.loqma.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "drivers", indexes = {
        @Index(name = "idx_drivers_online", columnList = "is_online")
})
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Driver extends User {

    @Column(name = "national_id", length = 30, nullable = false, unique = true)
    private String nationalId;

    @Column(name = "plate_number", length = 20, nullable = false, unique = true)
    private String plateNumber;

    @Column(name = "driving_license", length = 50, nullable = false, unique = true)
    private String drivingLicense;

    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "average_rating", nullable = false)
    private Double averageRating = 5.0;

    @Column(name = "cod_cash_balance", nullable = false)
    private Double codCashBalance = 0.0;

    @Column(name = "total_earnings", nullable = false)
    private Double totalEarnings = 0.0;
}