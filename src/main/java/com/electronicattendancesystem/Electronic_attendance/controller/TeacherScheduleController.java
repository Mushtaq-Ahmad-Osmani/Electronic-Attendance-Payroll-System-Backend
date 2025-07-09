package com.electronicattendancesystem.Electronic_attendance.controller;

import com.electronicattendancesystem.Electronic_attendance.dto.TeacherScheduleDto;
import com.electronicattendancesystem.Electronic_attendance.entity.TeacherSchedule;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.repository.TeacherScheduleRepo;
import com.electronicattendancesystem.Electronic_attendance.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/schedules")
public class TeacherScheduleController {
    @Autowired
    private TeacherScheduleRepo scheduleRepo;

    @Autowired
    private UsersManagementService usersManagementService;

    // Get all teacher schedules
    @GetMapping("/admin/all")
    public List<TeacherSchedule> getAllSchedules() {
        return scheduleRepo.findAll();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<TeacherSchedule>getTeacherScheduleById(@PathVariable Long id){
        Optional<TeacherSchedule> teacherSchedule = scheduleRepo.findById(id);
        return teacherSchedule.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());

    }


    // Create a new teacher schedule (Only admin can create)

    @PostMapping("/admin/add")
        public ResponseEntity<?> createSchedule (@RequestBody TeacherScheduleDto dto){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Teachers adminUser = usersManagementService.findByEmail(auth.getName()).orElse(null);
            if (adminUser == null || !adminUser.getRole().equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can create schedule.");
            }

            Teachers teacher = usersManagementService.findById(dto.getTeacherId())
                    .orElse(null);
            System.out.println("Teacher found: " + teacher);
        System.out.println("Is Teacher: " + (teacher != null && teacher.isTeacher()));
            if (teacher == null || !teacher.isTeacher()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid teacher ID.");
            }

        /// Check if the teacher already has a schedule on the same day
        DayOfWeek newScheduleDay = DayOfWeek.valueOf(dto.getDayOfWeek().toUpperCase());
        boolean alreadyHasSchedule = scheduleRepo
                .findByTeacher_Id(teacher.getId())
                .stream()
                .anyMatch(schedule -> schedule.getDayOfWeek().equals(newScheduleDay));

        if (alreadyHasSchedule) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("This teacher already has a class on this day. Only one class per day is allowed");
        }


            TeacherSchedule schedule = new TeacherSchedule();
            schedule.setTeacher(teacher);
            schedule.setDayOfWeek(newScheduleDay);
            schedule.setStartTime(LocalTime.parse(dto.getStartTime()));
            schedule.setEndTime(LocalTime.parse(dto.getEndTime()));
            schedule.setExpectedCredits(dto.getExpectedCredits());

            return ResponseEntity.ok(scheduleRepo.save(schedule));
        }

    // Update an existing teacher schedule (Only admin can update)
    @PutMapping("/admin/updateSchedule/{id}")
    public ResponseEntity<?> updateSchedule(@PathVariable Long id, @RequestBody TeacherSchedule schedule) {
        TeacherSchedule existingSchedule = scheduleRepo.findById(id).orElse(null);
        if (existingSchedule == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Schedule not found.");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Teachers user = usersManagementService.findByEmail(auth.getName()).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can update schedule.");
        }

        // Check if the teacher has another schedule on the same day (excluding the current one)
        List<TeacherSchedule> teacherSchedules = scheduleRepo.findByTeacher_Id(existingSchedule.getTeacher().getId());
        boolean duplicateScheduleExists = teacherSchedules.stream()
                .anyMatch(s -> !s.getId().equals(id) && s.getDayOfWeek().equals(schedule.getDayOfWeek()));

        if (duplicateScheduleExists) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("This teacher already has a class on this day. Only one class per day is allowed");
        }

        //
        existingSchedule.setDayOfWeek(schedule.getDayOfWeek());
        existingSchedule.setStartTime(schedule.getStartTime());
        existingSchedule.setEndTime(schedule.getEndTime());
        existingSchedule.setExpectedCredits(schedule.getExpectedCredits());

        scheduleRepo.save(existingSchedule);
        return ResponseEntity.ok(existingSchedule);
    };


    // Delete a teacher schedule (Only admin can delete)
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteSchedule(@PathVariable Long id) {
        TeacherSchedule schedule = scheduleRepo.findById(id).orElse(null);
        if (schedule == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Schedule not found.");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Teachers user = usersManagementService.findByEmail(auth.getName()).orElse(null);
        if (user == null || !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admin can delete schedule.");
        }

        scheduleRepo.delete(schedule);
        return ResponseEntity.ok("Schedule deleted successfully.");
    }


}


