package com.loqma.customer.repository;

import com.loqma.owner.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CustomerRestaurantRepository extends JpaRepository<Restaurant, String> {
}
