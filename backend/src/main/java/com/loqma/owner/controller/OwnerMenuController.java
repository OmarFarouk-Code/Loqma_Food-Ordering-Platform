package com.loqma.owner.controller;

import com.loqma.owner.dto.MenuDtos.MenuItemRequest;
import com.loqma.owner.dto.MenuDtos.MenuItemResponse;
import com.loqma.owner.dto.MenuDtos.StockUpdateRequest;
import com.loqma.owner.service.OwnerMenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/menu")
@RequiredArgsConstructor
public class OwnerMenuController {

    private final OwnerMenuService menuService;

    @GetMapping
    public List<MenuItemResponse> list(@RequestParam(required = false) String search,
                                       @RequestParam(defaultValue = "all") String stock) {
        return menuService.list(search, stock);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@Valid @RequestBody MenuItemRequest request) {
        return menuService.create(request);
    }

    @PutMapping("/{id}")
    public MenuItemResponse update(@PathVariable String id, @Valid @RequestBody MenuItemRequest request) {
        return menuService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        menuService.delete(id);
    }

    @PatchMapping("/{id}/stock")
    public MenuItemResponse setStock(@PathVariable String id,
                                     @Valid @RequestBody StockUpdateRequest request) {
        return menuService.setStock(id, request.available());
    }
}