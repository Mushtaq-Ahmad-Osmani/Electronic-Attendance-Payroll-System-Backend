package com.electronicattendancesystem.Electronic_attendance.repository;

import com.electronicattendancesystem.Electronic_attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
@Repository
public interface AttendanceRepo extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByEmailAndAttendanceTimeBetween(String email, LocalDateTime start, LocalDateTime end);




    List<Attendance> findByUserIdAndAttendanceDate(Long userId, LocalDate current);

    List<Attendance> findByUserId(Long id);

    List<Attendance> findByAttendanceTimeBetween(LocalDateTime atStartOfDay, LocalDateTime atStartOfDay1);

    List<Attendance> findByUser_IdAndAttendanceTimeBetween(Long userId, LocalDateTime atStartOfDay, LocalDateTime atStartOfDay1);


    List<Attendance> findAllByAttendanceDate(LocalDate today);

    List<Attendance> findAllByEmailAndAttendanceDateBetween(String email, LocalDate startDate, LocalDate endDate);
}
