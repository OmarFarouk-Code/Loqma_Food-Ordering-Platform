package com.loqma.customer.repository;

import com.loqma.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerFavoriteRepository extends JpaRepository<Customer, String> {

    @Query("select r.id from Customer c join c.favoriteRestaurants r " +
           "where c.id = :customerId and r.id in :restaurantIds")
    List<String> findFavoriteRestaurantIds(@Param("customerId") String customerId,
                                           @Param("restaurantIds") List<String> restaurantIds);

    @Query("select c from Customer c left join fetch c.favoriteRestaurants where c.id = :customerId")
    Optional<Customer> findWithFavoritesById(@Param("customerId") String customerId);
}