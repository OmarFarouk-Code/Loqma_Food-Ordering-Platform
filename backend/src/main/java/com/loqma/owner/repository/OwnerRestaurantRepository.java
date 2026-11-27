package com.loqma.owner.repository;

import com.loqma.owner.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRestaurantRepository extends JpaRepository<Restaurant, String> {
    Optional<Restaurant> findByOwnerId(String ownerId);
}