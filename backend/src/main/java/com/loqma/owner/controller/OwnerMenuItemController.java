package com.loqma.owner.controller;
import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.loqma.customer.repository.MenuItemRepository;
import com.loqma.owner.entity.MenuItem;
import com.loqma.owner.service.ImageStorageService;

@RestController
@RequestMapping("/api/owner/menu-items")
public class OwnerMenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final ImageStorageService imageStorageService;

    public OwnerMenuItemController(MenuItemRepository menuItemRepository,
                                   ImageStorageService imageStorageService) {
        this.menuItemRepository = menuItemRepository;
        this.imageStorageService = imageStorageService;
    }

    @PostMapping("/{id}/photo")
    public Map<String, String> uploadPhoto(@PathVariable String id,
                                           @RequestParam("file") MultipartFile file)
            throws IOException {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found"));

        String url = imageStorageService.upload(file, "menu-items");
        item.setPhoto(url);
        menuItemRepository.save(item);

        return Map.of("photo", url);
    }
}