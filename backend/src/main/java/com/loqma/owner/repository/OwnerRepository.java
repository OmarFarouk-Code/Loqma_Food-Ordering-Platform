package com.loqma.owner.repository;

import com.loqma.owner.entity.RestaurantOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OwnerRepository extends JpaRepository<RestaurantOwner, String> {
    Optional<RestaurantOwner> findByEmail(String email);
}