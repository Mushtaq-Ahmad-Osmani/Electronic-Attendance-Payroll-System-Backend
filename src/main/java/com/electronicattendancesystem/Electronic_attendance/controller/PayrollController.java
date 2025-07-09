package com.electronicattendancesystem.Electronic_attendance.controller;

import com.electronicattendancesystem.Electronic_attendance.entity.Payroll;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.service.PayrollService;
import com.electronicattendancesystem.Electronic_attendance.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payroll")
public class PayrollController {

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private UsersManagementService usersManagementService;


    @GetMapping("/my")
    public ResponseEntity<?> getMyPayroll(@RequestParam int year, @RequestParam int month) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Teachers currentUser = usersManagementService.findByEmail(email).orElse(null);
        if (currentUser == null || !currentUser.isTeacher()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only teachers can view their payroll.");
        }

        List<Payroll> myPayroll = payrollService.getPayrollForTeacherByMonth(currentUser, year, month);
        return ResponseEntity.ok(myPayroll);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllPayrollsForAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();


        Teachers currentUser = usersManagementService.findByEmail(email).orElse(null);
        if (currentUser == null || !currentUser.getRole().equals("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only admins can view the full payroll list.");
        }

        List<Payroll> payrolls = payrollService.getAllPayrolls();
        return ResponseEntity.ok(payrolls);
    }
    @PostMapping("/generate")
    public ResponseEntity<String> generatePayroll(@RequestParam int year, @RequestParam int month) {
        payrollService.generateMonthlyPayroll(year, month);
        return ResponseEntity.ok("Payroll generated successfully for " + month + "/" + year);
    }
    // Endpoint to view payroll for a specific month
    @GetMapping("/view")
    public ResponseEntity<List<Payroll>> getPayroll(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(payrollService.getPayrollForMonth(year, month));
    }
}
