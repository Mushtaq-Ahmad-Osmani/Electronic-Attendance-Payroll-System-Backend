package com.electronicattendancesystem.Electronic_attendance.repository;

import com.electronicattendancesystem.Electronic_attendance.entity.TeacherSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherScheduleRepo extends JpaRepository<TeacherSchedule,Long> {


    List<TeacherSchedule> findByTeacher_Id(Long userid);



}
