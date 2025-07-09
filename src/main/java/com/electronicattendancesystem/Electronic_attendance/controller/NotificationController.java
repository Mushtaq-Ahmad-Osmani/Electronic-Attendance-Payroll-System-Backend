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
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;


    @PostMapping("/admin/create")
    public ResponseEntity<Notification> createNotification(
            @RequestParam("title") String title,
            @RequestParam("message") String message,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {

            String uploadDir = System.getProperty("user.dir") + "/notification-images/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = imageFile.getOriginalFilename();
            File dest = new File(uploadDir + fileName);
            try {
                imageFile.transferTo(dest);  // Save the uploaded file
                imageUrl = "notification-images/" + fileName;
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).build();
            }
        }

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setImageUrl(imageUrl);

        return ResponseEntity.ok(notificationService.saveNotification(notification));
    }


    @GetMapping("/all")
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }
}