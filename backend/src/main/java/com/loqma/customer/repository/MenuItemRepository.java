package com.loqma.customer.repository;

import java.util.*;
import com.loqma.owner.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    List<MenuItem> findByRestaurant_IdOrderByCategoryAscNameAsc(String restaurantId);
}