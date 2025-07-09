package com.electronicattendancesystem.Electronic_attendance.controller;

import com.electronicattendancesystem.Electronic_attendance.dto.ReqRes;
import com.electronicattendancesystem.Electronic_attendance.entity.Attendance;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.repository.AttendanceRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.TeachersRepo;
import com.electronicattendancesystem.Electronic_attendance.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    @Autowired
    private UsersManagementService usersManagementService;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private TeachersRepo teachersRepo;



    @PostMapping("/scan")
    public ResponseEntity<ReqRes> scanQRCode(@RequestParam String email) {
        System.out.println("Request recived for Email " + email);
        return ResponseEntity.ok(usersManagementService.markAttendance(email));

    }
    @GetMapping("/admin/all")
    public List<Attendance> getAllAttendances() {
        return attendanceRepo.findAll();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Long id) {
        Optional<Attendance> attendance = attendanceRepo.findById(id);
        return attendance.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/admin/{teacherId}")
    public ResponseEntity<List<Attendance>> getAttendancesByTeacherId(@PathVariable Long teacherId) {
        List<Attendance> attendances = attendanceRepo.findByUserId(teacherId);
        return ResponseEntity.ok(attendances);
    }

    @GetMapping("/admin/this-month")
    public List<Attendance> getAttendancesForCurrentMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);

        return attendanceRepo.findByAttendanceTimeBetween(
                firstDayOfMonth.atStartOfDay(),
                firstDayOfNextMonth.atStartOfDay()
        );
    }

    @GetMapping("/admin/this-month/{userId}")
    public List<Attendance> getUserAttendancesForCurrentMonth(@PathVariable Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate firstDayOfNextMonth = firstDayOfMonth.plusMonths(1);

        return attendanceRepo.findByUser_IdAndAttendanceTimeBetween(
                userId,
                firstDayOfMonth.atStartOfDay(),
                firstDayOfNextMonth.atStartOfDay()
        );
    }


        @GetMapping("/my")
    public List<Attendance> getMyAttendances() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        Teachers currentUser = teachersRepo.findByEmail(email).orElseThrow();
        return attendanceRepo.findByUserId(currentUser.getId());
    }


}
