package com.loqma.customer.repository;

import com.loqma.owner.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerMenuItemRepository extends JpaRepository<MenuItem, String> {

    @Query("select distinct m.restaurant.id from MenuItem m " +
           "where m.discountPercent > 0 and m.isAvailable = true " +
           "and m.restaurant.id in :restaurantIds")
    List<String> findRestaurantIdsWithDeals(@Param("restaurantIds") List<String> restaurantIds);
}