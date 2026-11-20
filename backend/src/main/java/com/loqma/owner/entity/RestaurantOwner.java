package com.loqma.owner.entity;

import com.loqma.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "restaurant_owners")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantOwner extends User {

    @Column(name = "restaurant_name", length = 100, nullable = false)
    private String restaurantName;

    @Column(length = 100, nullable = false)
    private String branch;

    @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL)
    private Restaurant restaurant;
}