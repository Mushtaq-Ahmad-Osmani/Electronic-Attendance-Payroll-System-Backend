package com.electronicattendancesystem.Electronic_attendance.controller;

import com.electronicattendancesystem.Electronic_attendance.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-scheduler")
public class ScheduledTestController {


    @Autowired
    private UsersManagementService usersManagementService;

    @PostMapping("/run-checkout-eval")
    public String runCheckoutEval() {
        usersManagementService.evaluateLateOrMissingCheckOuts();
        return "Checkouts evaluated manually";
    }

    @GetMapping("/run-absent-check")
    public String runAbsentCheck() {
        usersManagementService.markAbsentTeachersAutomatically();
        return "Absent teachers marked manually";
    }
}



