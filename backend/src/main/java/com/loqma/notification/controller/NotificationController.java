package com.loqma.notification.controller;

import com.loqma.notification.dto.NotificationRequest;
import com.loqma.notification.dto.NotificationResponse;
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
    public NotificationResponse create(@RequestBody NotificationRequest request) {
        return notificationService.create(request);
    }

    @GetMapping
    public List<NotificationResponse> getForUser(@RequestParam String userId) {
        return notificationService.getForUser(userId);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(@PathVariable String id) {
        return notificationService.markAsRead(id);
    }
}