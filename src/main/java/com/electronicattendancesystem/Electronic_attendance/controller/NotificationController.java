package com.electronicattendancesystem.Electronic_attendance.controller;

import com.electronicattendancesystem.Electronic_attendance.entity.Notification;
import com.electronicattendancesystem.Electronic_attendance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    private static final String UPLOAD_DIR = "C:/notification-images/";

    @PostMapping("/admin/create")
    public ResponseEntity<Notification> createNotification(
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);

        if (image != null && !image.isEmpty()) {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            File dest = new File(UPLOAD_DIR + fileName);
            try {
                image.transferTo(dest);
                notification.setImagePath("/notification-images/" + fileName); // Match static resource path
            } catch (IOException e) {

                return ResponseEntity.status(500).build();
            }
        }

        return ResponseEntity.ok(notificationService.createNotification(notification));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted successfully");
    }
}