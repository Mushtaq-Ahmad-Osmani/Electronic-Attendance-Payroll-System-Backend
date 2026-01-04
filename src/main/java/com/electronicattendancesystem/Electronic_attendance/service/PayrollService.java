package com.electronicattendancesystem.Electronic_attendance.service;

import com.electronicattendancesystem.Electronic_attendance.entity.Attendance;
import com.electronicattendancesystem.Electronic_attendance.entity.Payroll;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.repository.AttendanceRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.PayrollRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.TeachersRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
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

    // Automatically generate monthly payroll
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

            double creditRate = teacher.getCreditRate(); // Credit rate from the Teacher class
            double grossSalary = totalCredits * creditRate;
            double tax = grossSalary * 0.1; // 10% tax
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

    public void exportPayrollAsCsvForTeacher(Teachers teacher, int year, int month, HttpServletResponse response) throws Exception {
        List<Payroll> payrolls = payrollRepo.findByTeacherAndYearAndMonth(teacher, year, month);
        if (payrolls.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"No payroll data found for year: " + year + ", month: " + month + "\"}");
            return;
        }

        response.setHeader("Content-Disposition", "attachment; filename=my-payroll-" + year + "-" + month + ".csv");
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (OutputStream outputStream = response.getOutputStream()) {
            // Write BOM for Persian character support
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // Write data to CSV using OutputStream
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Name,Email,Month,Year,Credits,Credit Rate,Gross,Tax,Net\n");
            for (Payroll payroll : payrolls) {
                csvContent.append(String.format("%s,%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f\n",
                        escapeCsv(payroll.getTeacher().getName()),
                        escapeCsv(payroll.getTeacher().getEmail()),
                        payroll.getMonth(),
                        payroll.getYear(),
                        payroll.getTotalCredits(),
                        payroll.getCreditRate(),
                        payroll.getGrossSalary(),
                        payroll.getTax(),
                        payroll.getNetSalary()));
            }
            outputStream.write(csvContent.toString().getBytes("UTF-8"));
            outputStream.flush();
        }
    }

    public void exportPayrollAsExcelForTeacher(Teachers teacher, int year, int month, HttpServletResponse response) throws IOException {
        List<Payroll> payrolls = payrollRepo.findByTeacher(teacher);

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet("Payroll");

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=my-payroll.xlsx");

        Row header = sh.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Email");
        header.createCell(2).setCellValue("Month");
        header.createCell(3).setCellValue("Year");
        header.createCell(4).setCellValue("Credits");
        header.createCell(5).setCellValue("Gross");
        header.createCell(6).setCellValue("Tax");
        header.createCell(7).setCellValue("Net");

        int row = 1;
        for (Payroll p : payrolls) {
            Row r = sh.createRow(row++);
            r.createCell(0).setCellValue(p.getTeacher().getName());
            r.createCell(1).setCellValue(p.getTeacher().getEmail());
            r.createCell(2).setCellValue(p.getMonth());
            r.createCell(3).setCellValue(p.getYear());
            r.createCell(4).setCellValue(p.getTotalCredits());
            r.createCell(5).setCellValue(p.getGrossSalary());
            r.createCell(6).setCellValue(p.getTax());
            r.createCell(7).setCellValue(p.getNetSalary());
        }

        wb.write(response.getOutputStream());
        wb.close();
    }

    public void exportAllPayrollsAsCsv(HttpServletResponse response, Integer month, Integer year) throws Exception {
        if (month == null || year == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"Year and month are required for export.\"}");
            return;
        }

        List<Payroll> payrolls = payrollRepo.findByYearAndMonth(year, month);
        if (payrolls.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"No payroll data found for year: " + year + ", month: " + month + "\"}");
            return;
        }

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"payrolls-" + year + "-" + month + ".csv\"");
        response.setCharacterEncoding("UTF-8");

        try (OutputStream outputStream = response.getOutputStream()) {
            // Write BOM for Persian character support
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // Write data to CSV using OutputStream
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("ID,Teacher Name,Email,Year,Month,Total Credits,Credit Rate,Gross Salary,Tax,Net Salary\n");
            for (Payroll payroll : payrolls) {
                csvContent.append(String.format("%d,%s,%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f\n",
                        payroll.getId(),
                        escapeCsv(payroll.getTeacher().getName()),
                        escapeCsv(payroll.getTeacher().getEmail()),
                        payroll.getYear(),
                        payroll.getMonth(),
                        payroll.getTotalCredits(),
                        payroll.getCreditRate(),
                        payroll.getGrossSalary(),
                        payroll.getTax(),
                        payroll.getNetSalary()));
            }
            outputStream.write(csvContent.toString().getBytes("UTF-8"));
            outputStream.flush();
        }
    }

    // Helper method for formatting CSV values
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public void exportAllPayrollsAsExcel(HttpServletResponse response, Integer month, Integer year) throws Exception {
        if (month == null || year == null) {
            throw new IllegalArgumentException("Year and month are required for export.");
        }

        List<Payroll> payrolls = payrollRepo.findByYearAndMonth(year, month);
        if (payrolls.isEmpty()) {
            throw new IllegalStateException("No payroll data found for year: " + year + ", month: " + month);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"payrolls-" + year + "-" + month + ".xlsx\"");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Payrolls");

        // Create header
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Teacher Name", "Email", "Year", "Month", "Total Credits", "Credit Rate", "Gross Salary", "Tax", "Net Salary"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }

        // Populate data
        int rowNum = 1;
        for (Payroll payroll : payrolls) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(payroll.getId());
            row.createCell(1).setCellValue(payroll.getTeacher().getName());
            row.createCell(2).setCellValue(payroll.getTeacher().getEmail());
            row.createCell(3).setCellValue(payroll.getYear());
            row.createCell(4).setCellValue(payroll.getMonth());
            row.createCell(5).setCellValue(payroll.getTotalCredits());
            row.createCell(6).setCellValue(payroll.getCreditRate());
            row.createCell(7).setCellValue(payroll.getGrossSalary());
            row.createCell(8).setCellValue(payroll.getTax());
            row.createCell(9).setCellValue(payroll.getNetSalary());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to output
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportPayrollAsCsvForTeacherByAdmin(Long teacherId, int year, int month, HttpServletResponse response) throws Exception {
        Teachers teacher = teachersRepo.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        List<Payroll> payrolls = payrollRepo.findByTeacherIdAndYearAndMonth(teacherId, year, month);
        if (payrolls.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\": \"No payroll data found for teacher ID: " + teacherId + ", year: " + year + ", month: " + month + "\"}");
            return;
        }

        response.setHeader("Content-Disposition", "attachment; filename=teacher-payroll-" + teacherId + "-" + year + "-" + month + ".csv");
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try (OutputStream outputStream = response.getOutputStream()) {
            // Write BOM for Persian character support
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // Write data to CSV using OutputStream
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("Name,Email,Month,Year,Credits,Credit Rate,Gross,Tax,Net\n");
            for (Payroll payroll : payrolls) {
                csvContent.append(String.format("%s,%s,%d,%d,%d,%.2f,%.2f,%.2f,%.2f\n",
                        escapeCsv(payroll.getTeacher().getName()),
                        escapeCsv(payroll.getTeacher().getEmail()),
                        payroll.getMonth(),
                        payroll.getYear(),
                        payroll.getTotalCredits(),
                        payroll.getCreditRate(),
                        payroll.getGrossSalary(),
                        payroll.getTax(),
                        payroll.getNetSalary()));
            }
            outputStream.write(csvContent.toString().getBytes("UTF-8"));
            outputStream.flush();
        }
    }

    public void exportPayrollAsExcelForTeacherByAdmin(Long teacherId, int year, int month, HttpServletResponse response) throws IOException {
        Teachers teacher = teachersRepo.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sh = wb.createSheet("Teacher Payroll");

        List<Payroll> payrolls = payrollRepo.findByTeacherIdAndYearAndMonth(teacherId, year, month);

        Row header = sh.createRow(0);
        header.createCell(0).setCellValue("Name");
        header.createCell(1).setCellValue("Email");
        header.createCell(2).setCellValue("Month");
        header.createCell(3).setCellValue("Year");
        header.createCell(4).setCellValue("Credits");
        header.createCell(5).setCellValue("Gross");
        header.createCell(6).setCellValue("Tax");
        header.createCell(7).setCellValue("Net");

        int row = 1;
        for (Payroll p : payrolls) {
            Row r = sh.createRow(row++);
            r.createCell(0).setCellValue(p.getTeacher().getName());
            r.createCell(1).setCellValue(p.getTeacher().getEmail());
            r.createCell(2).setCellValue(p.getMonth());
            r.createCell(3).setCellValue(p.getYear());
            r.createCell(4).setCellValue(p.getTotalCredits());
            r.createCell(5).setCellValue(p.getGrossSalary());
            r.createCell(6).setCellValue(p.getTax());
            r.createCell(7).setCellValue(p.getNetSalary());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=teacher-payroll.xlsx");

        wb.write(response.getOutputStream());
        wb.close();
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

    public List<Payroll> getPayrollsByTeacher(Long id, int year, int month) {
        return payrollRepo.findByTeacherIdAndYearAndMonth(id, year, month);
    }
}