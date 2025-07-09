package com.electronicattendancesystem.Electronic_attendance.repository;

import com.electronicattendancesystem.Electronic_attendance.entity.Payroll;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayrollRepo extends JpaRepository<Payroll, Long> {
    List<Payroll> findByYearAndMonth(int year, int month);



    List<Payroll> findByTeacherAndYearAndMonth(Teachers teacher, int year, int month);
}