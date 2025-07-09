package com.electronicattendancesystem.Electronic_attendance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Data
public class TeacherSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id" )
    @ToString.Exclude
    private Teachers teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;


    private int expectedCredits;




}



