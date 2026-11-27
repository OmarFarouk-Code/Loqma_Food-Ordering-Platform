package com.loqma.customer.repository;

import com.loqma.customer.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerReviewRepository extends JpaRepository<Review, String> {

    @Query("select r from Review r where r.restaurant.id = :restaurantId")
    Page<Review> findByRestaurant(@Param("restaurantId") String restaurantId, Pageable pageable);

    @Query("select avg(r.restaurantRating) from Review r where r.restaurant.id = :restaurantId")
    Double findAverageRating(@Param("restaurantId") String restaurantId);

    @Query("select r.restaurantRating, count(r) from Review r " +
           "where r.restaurant.id = :restaurantId group by r.restaurantRating")
    List<Object[]> countByRating(@Param("restaurantId") String restaurantId);
}