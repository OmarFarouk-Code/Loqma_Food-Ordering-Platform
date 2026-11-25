package com.loqma.customer.repository;

import com.loqma.owner.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRestaurantRepository extends JpaRepository<Restaurant, String> {

    @Query("""
        select r from Restaurant r
        where (:cuisine is null or lower(r.cuisine) = lower(:cuisine))
          and (:minRating is null or r.averageRating >= :minRating)
          and (:search is null
               or lower(r.name) like lower(concat('%', :search, '%'))
               or exists (select 1 from MenuItem m
                          where m.restaurant = r
                            and lower(m.name) like lower(concat('%', :search, '%'))))
          and (:dealsOnly = false
               or exists (select 1 from MenuItem d
                          where d.restaurant = r
                            and d.discountPercent > 0
                            and d.isAvailable = true))
        order by r.averageRating desc
        """)
    List<Restaurant> findFiltered(@Param("cuisine") String cuisine,
                                  @Param("minRating") Double minRating,
                                  @Param("search") String search,
                                  @Param("dealsOnly") boolean dealsOnly);
}