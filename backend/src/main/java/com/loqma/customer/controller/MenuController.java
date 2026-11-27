package com.loqma.customer.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loqma.customer.dto.MenuResponse;
import com.loqma.customer.service.MenuService;

@RestController
@RequestMapping("/api/restaurants")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/{restaurantId}/menu")
    public MenuResponse getMenu(@PathVariable String restaurantId) {
        return menuService.getMenu(restaurantId);
    }
}