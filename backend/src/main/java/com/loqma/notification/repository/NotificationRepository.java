package com.loqma.notification.repository;

import com.loqma.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByUser_IdOrderByTimestampDesc(String userId);
}