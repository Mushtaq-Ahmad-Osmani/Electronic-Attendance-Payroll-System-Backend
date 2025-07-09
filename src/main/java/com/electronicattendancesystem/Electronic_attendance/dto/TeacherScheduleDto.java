package com.electronicattendancesystem.Electronic_attendance.dto;


import lombok.Data;

@Data
public class TeacherScheduleDto {
    private Long teacherId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private int expectedCredits;
}
