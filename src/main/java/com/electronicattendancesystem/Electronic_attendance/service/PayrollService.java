// PayrollService.java
package com.electronicattendancesystem.Electronic_attendance.service;

import com.electronicattendancesystem.Electronic_attendance.entity.Attendance;
import com.electronicattendancesystem.Electronic_attendance.entity.Payroll;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.repository.AttendanceRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.PayrollRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.TeachersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    @Autowired
    private TeachersRepo teachersRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Autowired
    private PayrollRepo payrollRepo;


    //Auto GenerateMonthly Payroll
    @Scheduled(cron = "0 0 0 1 * *")
    public void autoGenerateMonthlyPayroll() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        generateMonthlyPayroll(year, month);
    }




    public void generateMonthlyPayroll(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Teachers> allTeachers = teachersRepo.findAll()
                .stream()
                .filter(Teachers::isTeacher)
                .collect(Collectors.toList());

        for (Teachers teacher : allTeachers) {
            List<Attendance> teacherAttendances = attendanceRepo
                    .findAllByEmailAndAttendanceDateBetween(
                            teacher.getEmail(),
                            startDate,
                            endDate
                    );

            int totalCredits = teacherAttendances.stream()
                    .mapToInt(Attendance::getActualCredits)
                    .sum();

            double creditRate = teacher.getCreditRate(); // نرخ هر کردیت از کلاس Teacher
            double grossSalary = totalCredits * creditRate;
            double tax = grossSalary * 0.1; // 10٪ مالیه
            double netSalary = grossSalary - tax;

            Payroll payroll = new Payroll();
            payroll.setTeacher(teacher);
            payroll.setMonth(month);
            payroll.setYear(year);
            payroll.setTotalCredits(totalCredits);
            payroll.setCreditRate(creditRate);
            payroll.setGrossSalary(grossSalary);
            payroll.setTax(tax);
            payroll.setNetSalary(netSalary);

            payrollRepo.save(payroll);
        }
    }

    public List<Payroll> getPayrollForMonth(int year, int month) {
        return payrollRepo.findByYearAndMonth(year, month);
    }


    public List<Payroll> getAllPayrolls() {
        return payrollRepo.findAll();
    }

    public List<Payroll> getPayrollForTeacherByMonth(Teachers teacher, int year, int month) {
        return payrollRepo.findByTeacherAndYearAndMonth(teacher, year, month);
    }
}