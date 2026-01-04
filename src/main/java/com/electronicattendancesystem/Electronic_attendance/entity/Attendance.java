package com.electronicattendancesystem.Electronic_attendance.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
@Entity
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String userName;

    private String email;



    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private Teachers user;
    private LocalDateTime attendanceTime;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private boolean isAbsent;
    private LocalDate attendanceDate;
    @ManyToOne
    @JoinColumn(name = "schedule_id",nullable = true)
    private TeacherSchedule schedule;


    private int expectedCredits;


    private int actualCredits;




    public void setAttendanceDate(LocalDate today) {
        this.attendanceDate = today;
    }


}