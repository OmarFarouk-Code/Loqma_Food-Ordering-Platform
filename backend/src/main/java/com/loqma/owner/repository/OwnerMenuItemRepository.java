package com.loqma.owner.repository;

import com.loqma.owner.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerMenuItemRepository extends JpaRepository<MenuItem, String> {
    List<MenuItem> findByRestaurantIdOrderByCategoryAscNameAsc(String restaurantId);

    // Ownership check: an item is only found if it belongs to this restaurant
    Optional<MenuItem> findByIdAndRestaurantId(String id, String restaurantId);
}