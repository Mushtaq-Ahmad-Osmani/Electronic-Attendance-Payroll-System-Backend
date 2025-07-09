package com.electronicattendancesystem.Electronic_attendance.repository;

import com.electronicattendancesystem.Electronic_attendance.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}