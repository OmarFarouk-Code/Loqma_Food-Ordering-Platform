package com.loqma.customer.repository;

import com.loqma.owner.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RestaurantRepository extends JpaRepository<Restaurant, String> {
}
