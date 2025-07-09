package com.electronicattendancesystem.Electronic_attendance.service;

import com.electronicattendancesystem.Electronic_attendance.entity.Notification;
import com.electronicattendancesystem.Electronic_attendance.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification saveNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}