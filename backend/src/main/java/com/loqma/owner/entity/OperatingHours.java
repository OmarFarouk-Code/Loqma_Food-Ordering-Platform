package com.loqma.owner.entity;

import com.loqma.common.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "operating_hours", indexes = {
        @Index(name = "idx_op_hours_day", columnList = "restaurant_id, day_of_week"),
        @Index(name = "idx_op_hours_special", columnList = "is_special_day, special_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 20, nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_day_off", nullable = false)
    @Builder.Default
    private Boolean isDayOff = false;

    @Column(name = "is_24_hours", nullable = false)
    @Builder.Default
    private Boolean is24Hours = false;

    @Column(name = "is_special_day", nullable = false)
    @Builder.Default
    private Boolean isSpecialDay = false;

    @Column(name = "special_date")
    private LocalDate specialDate;
}