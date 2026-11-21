package com.loqma.customer.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.loqma.customer.dto.CategoryDto;
import com.loqma.customer.dto.ExtraDto;
import com.loqma.customer.dto.ItemDto;
import com.loqma.customer.dto.MenuResponse;
import com.loqma.customer.dto.SizeDto;
import com.loqma.customer.repository.MenuItemRepository;
import com.loqma.customer.repository.RestaurantRepository;
import com.loqma.owner.entity.MenuItem;


@Service
public class MenuService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuService(RestaurantRepository restaurantRepository,
                       MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional(readOnly = true)
    public MenuResponse getMenu(String restaurantId) {

        // 1. Make sure the restaurant exists, otherwise answer 404
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found");
        }

        // 2. Load all menu items of this restaurant (already sorted)
        List<MenuItem> items =
                menuItemRepository.findByRestaurant_IdOrderByCategoryAscNameAsc(restaurantId);

        // 3. Group the flat list by category name
        Map<String, List<MenuItem>> itemsByCategory = items.stream()
                .collect(Collectors.groupingBy(
                        MenuItem::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 4. Turn each group into a CategoryDto
        List<CategoryDto> categories = itemsByCategory.entrySet().stream()
                .map(entry -> CategoryDto.builder()
                        .name(entry.getKey())
                        .items(entry.getValue().stream().map(this::toItemDto).toList())
                        .build())
                .toList();

        return MenuResponse.builder().categories(categories).build();
    }

    // Converts one entity (database row) into one DTO (JSON shape)
    private ItemDto toItemDto(MenuItem item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getBasePrice())
                .photo(item.getPhoto())
                .available(item.getIsAvailable())
                .sizes(item.getSizes().stream()
                        .map(size -> SizeDto.builder()
                                .id(size.getId())
                                .name(size.getSizeName())
                                .price(size.getPrice())
                                .build())
                        .toList())
                .extras(item.getExtras().stream()
                        .map(extra -> ExtraDto.builder()
                                .id(extra.getId())
                                .name(extra.getName())
                                .price(extra.getPrice())
                                .build())
                        .toList())
                .build();
    }
}