package com.loqma.notification.controller;

import com.loqma.notification.dto.NotificationRequest;
import com.loqma.notification.entity.Notification;
import com.loqma.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public Notification create(@RequestBody NotificationRequest request) {
        return notificationService.create(request.userId(), request.message());
    }

    @GetMapping
    public List<Notification> getForUser(@RequestParam Long userId) {
        return notificationService.getForUser(userId);
    }

    @PatchMapping("/{id}/read")
    public Notification markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}