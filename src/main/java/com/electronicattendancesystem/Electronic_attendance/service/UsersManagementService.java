package com.electronicattendancesystem.Electronic_attendance.service;

import com.electronicattendancesystem.Electronic_attendance.dto.ReqRes;
import com.electronicattendancesystem.Electronic_attendance.entity.Attendance;
import com.electronicattendancesystem.Electronic_attendance.entity.TeacherSchedule;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.repository.AttendanceRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.TeacherScheduleRepo;
import com.electronicattendancesystem.Electronic_attendance.repository.TeachersRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsersManagementService {

    @Autowired
    private TeachersRepo teachersRepo;

    @Autowired
    private TeacherScheduleRepo teacherScheduleRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ReqRes register(ReqRes registrationRequest) {
        ReqRes resp = new ReqRes();
        resp.setCredits(registrationRequest.getCredits());
        resp.setCreditRate(registrationRequest.getCreditRate());
        try{

            Teachers teachers = new Teachers();
            teachers.setEmail(registrationRequest.getEmail());
            teachers.setPhone(registrationRequest.getPhone());
            teachers.setRole(registrationRequest.getRole());
            teachers.setName(registrationRequest.getName());
            teachers.setGender(registrationRequest.getGender());
            teachers.setTeacher(true);
            teachers.setCredits(registrationRequest.getCredits());
            teachers.setCreditRate(registrationRequest.getCreditRate());
            teachers.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

            String token = UUID.randomUUID().toString();  // Generate a unique verification token
            teachers.setVerificationToken(token);
            teachers.setEmailVerified(false);


            Teachers teachersResult = teachersRepo.save(teachers);

            if (teachersResult.getId() > 0) {

                // Create verification link
                String verificationLink = "http://localhost:8080/auth/verify?token=" + token;


                emailService.sendEmail(
                        teachersResult.getEmail(),
                        "Email Verification",
                        "Dear " + teachersResult.getName() + ",\n\nPlease verify your email by clicking the link below:\n" + verificationLink
                );

                resp.setTeachers(teachersResult);
                resp.setTeacher(teachersResult.isTeacher());
                resp.setMessage("User saved successfully. Verification email sent.");
                resp.setStatusCode(200);
            }

        } catch (Exception e) {
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }
        return resp;
    }

    public ReqRes login(ReqRes loginRequest) {
        ReqRes response = new ReqRes();
        try {
            authenticationManager.
                    authenticate((new UsernamePasswordAuthenticationToken
                    (loginRequest.getEmail(), loginRequest.getPassword()))); // Authenticate the user
            var user = teachersRepo.findByEmail(loginRequest.getEmail()).orElseThrow();
            var jwt = jwtUtils.generateToken(user);
            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user); // Generate refresh token
            response.setStatusCode(200);
            response.setToken(jwt);
            response.setRole(user.getRole());
            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");
            response.setTeacher(user.isTeacher());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }
    // Refresh the authentication token
    public ReqRes refreshToken(ReqRes refreshTokenRequest) {
        ReqRes response = new ReqRes();
        try {
            String ourEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
            Teachers  teachers= teachersRepo.findByEmail(ourEmail).orElseThrow();
            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(),teachers )) {
                var jwt = jwtUtils.generateToken(teachers);
                response.setStatusCode(200);
                response.setToken(jwt);
                response.setRefreshToken(refreshTokenRequest.getToken());
                response.setExpirationTime("One Week");
                response.setMessage("Successfully Refreshed Token");
            }
            response.setStatusCode(200);
            return response;
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    public ReqRes getAllUsers() {
        ReqRes reqRes = new ReqRes();
        try {
            List<Teachers> result = teachersRepo.findAll();
            if (!result.isEmpty()) {
                reqRes.setTeachersList(result);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("No Users Found");
            }
            return reqRes;
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
            return reqRes;
        }
    }

    public ReqRes getUserById(Long id) {
        ReqRes reqRes = new ReqRes();
        try {
            Teachers usersById = teachersRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            reqRes.setTeachers(usersById);
            reqRes.setStatusCode(200);
            reqRes.setMessage("Users with id " + id + "found successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred: " + e.getMessage());
        }
        return reqRes;
    }


    public ReqRes deleteUser(Long id) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Teachers> usersOptional = teachersRepo.findById(id);

            if (usersOptional.isPresent()) {
//                OurUsers teacher = usersOptional.get();
//                reqRes.setCredits(teacher.getCredits());
//                reqRes.setCreditRate((int) teacher.getCreditRate());
                  teachersRepo.deleteById(id);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User deleted successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for deleting ");

            }

        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while deleting user " + e.getMessage());
        }

        reqRes.setTeacher(null);


        return reqRes;
    }

    public ReqRes updateUser(Long id, Teachers updatedUser) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Teachers> usersOptional = teachersRepo.findById(id);
            if (usersOptional.isPresent()) {
                Teachers existingUser = usersOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setPhone(updatedUser.getPhone());
                existingUser.setName(updatedUser.getName());
                existingUser.setRole(updatedUser.getRole());
                existingUser.setGender(updatedUser.getGender());
                existingUser.setCredits(updatedUser.getCredits());
                existingUser.setCreditRate(updatedUser.getCreditRate());
                existingUser.setTeacher(updatedUser.isTeacher());

                //Check if password is present in request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    //Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }
                Teachers savedUser = teachersRepo.save(existingUser);
                reqRes.setTeachers(savedUser);
                reqRes.setStatusCode(200);
                reqRes.setMessage("User updated successfully");
            } else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found for updating");
            }
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while updating user " + e.getMessage());
        }
    return reqRes;

    }
    public ReqRes getMyInfo(String email){
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Teachers> usersOptional = teachersRepo.findByEmail(email);
            if (usersOptional.isPresent()){
                reqRes.setTeachers(usersOptional.get());
                reqRes.setStatusCode(200);
                reqRes.setMessage("Successfully");
            }else {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found");
            }
        }catch (Exception e){
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error occurred while getting user : " + e.getMessage());
        }
        return reqRes;
    }

    public ReqRes uploadProfilePicture(Long userId, MultipartFile file) {
        ReqRes reqRes = new ReqRes();
        try {

            Teachers user = teachersRepo.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));


            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Generate unique filename
            Path path = Paths.get(uploadDir + fileName);


            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


            String imageUrl = "http://localhost:8080/uploads/" + fileName;
            user.setProfilePicture(imageUrl);

            //in Response
            reqRes.setProfilePicture(imageUrl);
            reqRes.setTeachers(user); // Set updated user in response

            teachersRepo.save(user);

            reqRes.setStatusCode(200);
            reqRes.setMessage("Profile picture uploaded successfully");
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error uploading profile picture: " + e.getMessage());
        }
        return reqRes;
    }
    // Mark attendance (check-in or check-out) for a user
    public ReqRes markAttendance(String email) {
        ReqRes reqRes = new ReqRes();
        try {
            Optional<Teachers> userOptional = teachersRepo.findByEmail(email);
            if (userOptional.isEmpty()) {
                reqRes.setStatusCode(404);
                reqRes.setMessage("User not found with email: " + email);
                return reqRes;
            }

            Teachers user = userOptional.get();
            LocalDate today = LocalDate.now();
            DayOfWeek todayDay = today.getDayOfWeek();
            List<TeacherSchedule> schedules = teacherScheduleRepo.findByTeacher_Id(user.getId());


            Optional<TeacherSchedule> todaySchedule = schedules.stream()
                    .filter(schedule -> schedule.getDayOfWeek().equals(todayDay))
                    .findFirst(); // Find schedule for today
            //for check-in after end-time
            if (todaySchedule.isPresent()) {
                TeacherSchedule schedule = todaySchedule.get();
                LocalTime classEndTime = schedule.getEndTime();
                if (LocalTime.now().isAfter(classEndTime)) {
                    reqRes.setStatusCode(400);
                    reqRes.setMessage("You are not allowed to check-in after the class ends");
                    return reqRes;
                }
            }


            Optional<LocalTime> optionalFirstClassStart = schedules.stream()
                    .filter(schedule -> schedule.getDayOfWeek().equals(todayDay))
                    .map(TeacherSchedule::getStartTime)
                    .min(LocalTime::compareTo); // Find the earliest start time

            Optional<Attendance> todayAttendance = attendanceRepo
                    .findByEmailAndAttendanceTimeBetween(
                            email,
                            today.atStartOfDay(),
                            today.plusDays(1).atStartOfDay()
                    );  // Check if attendance exists for today


            if (todayAttendance.isPresent()) {
                Attendance attendance = todayAttendance.get();

                if (attendance.getCheckOutTime() != null) {
                    reqRes.setStatusCode(400);
                    reqRes.setMessage("You have already checked out today at " + attendance.getCheckOutTime());
                    return reqRes;
                }

                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(attendance.getCheckInTime(), now);
                if (duration.toMinutes() < 30) {
                    reqRes.setStatusCode(400);
                    reqRes.setMessage("Check-out is only allowed at least 30 minutes after check-in. Please wait.");
                    return reqRes;
                }

                attendance.setCheckOutTime(now);
                attendance.setAttendanceDate(today);
                attendanceRepo.save(attendance);
                reqRes.setStatusCode(200);
                reqRes.setMessage("Check-out marked successfully at " + now);
                return reqRes;
            } else {
                Attendance attendance = new Attendance();
                attendance.setUser(user);
                attendance.setUserName(user.getName());
                attendance.setEmail(user.getEmail());
                todaySchedule.ifPresent(attendance::setSchedule);
                attendance.setAttendanceDate(today);
                attendance.setAttendanceTime(LocalDateTime.now());
                attendance.setCheckInTime(LocalDateTime.now());

                if (optionalFirstClassStart.isPresent()) {
                    LocalTime firstClassStartTime = optionalFirstClassStart.get();
                    LocalTime actualCheckIn = attendance.getCheckInTime().toLocalTime();
                    Duration lateDuration = Duration.between(firstClassStartTime, actualCheckIn);
                    boolean isAbsent = lateDuration.toMinutes() > 15;
                    attendance.setAbsent(isAbsent);

                    if (isAbsent && user.getCredits() > 0) {
                        user.setCredits(user.getCredits() - 1);
                        teachersRepo.save(user);
                    }
                } else {
                    attendance.setAbsent(false);  // No schedule, so not absent
                }

                long totalMinutes = schedules.stream()
                        .filter(schedule -> schedule.getDayOfWeek().equals(todayDay))
                        .mapToLong(schedule -> Duration.between(schedule.getStartTime(), schedule.getEndTime()).toMinutes())
                        .sum();   // Calculate total minutes of classes

                int expectedCredits = (int) Math.round(totalMinutes / 60.0);
                attendance.setExpectedCredits(expectedCredits);

                int actualCredits = attendance.isAbsent() ? Math.max(0, expectedCredits - 1) : expectedCredits;
                attendance.setActualCredits(actualCredits);




                attendanceRepo.save(attendance);



                reqRes.setStatusCode(200);
                reqRes.setMessage("Check-in marked successfully at " + attendance.getCheckInTime());
                return reqRes;
            }
        } catch (Exception e) {
            reqRes.setStatusCode(500);
            reqRes.setMessage("Error while marking attendance: " + e.getMessage());
            return reqRes;
        }
    }
    // Scheduled task to automatically mark absent teachers at 11:59 PM
    @Scheduled(cron = "0 59 23 * * *")
    public void markAbsentTeachersAutomatically() {
        LocalDate today = LocalDate.now();
        DayOfWeek todayDay = today.getDayOfWeek();

        List<Teachers> teachers = teachersRepo.findAll().stream()
                .filter(Teachers::isTeacher)
                .collect(Collectors.toList());

        for (Teachers teacher : teachers) {
            List<TeacherSchedule> schedules = teacherScheduleRepo.findByTeacher_Id(teacher.getId());

            boolean hasClassToday = schedules.stream()
                    .anyMatch(schedule -> schedule.getDayOfWeek().equals(todayDay));

            if (!hasClassToday) continue;

            Optional<Attendance> attendanceOpt = attendanceRepo
                    .findByEmailAndAttendanceTimeBetween(
                            teacher.getEmail(),
                            today.atStartOfDay(),
                            today.plusDays(1).atStartOfDay()
                    );

            if (attendanceOpt.isEmpty()) {
                Attendance absentAttendance = new Attendance();

                absentAttendance.setUser(teacher);
                absentAttendance.setUserName(teacher.getName());
                absentAttendance.setEmail(teacher.getEmail());
                absentAttendance.setAttendanceTime(LocalDateTime.now());
                absentAttendance.setCheckInTime(null);
                absentAttendance.setCheckOutTime(null);
                absentAttendance.setAttendanceDate(today);
                absentAttendance.setAbsent(true);

                // Calculate total minutes of classes today
                long totalMinutes = schedules.stream()
                        .filter(s -> s.getDayOfWeek().equals(todayDay))
                        .mapToLong(s -> Duration.between(s.getStartTime(), s.getEndTime()).toMinutes())
                        .sum();
                int expectedCredits = (int) Math.round(totalMinutes / 60.0);

                absentAttendance.setExpectedCredits(expectedCredits);
                absentAttendance.setActualCredits(0);

                attendanceRepo.save(absentAttendance);

                // Deduct expected credits from the teacher
                int updatedCredits = Math.max(0, teacher.getCredits() - expectedCredits);
                teacher.setCredits(updatedCredits);
                teachersRepo.save(teacher);
            }
        }
    }
    // Scheduled task to evaluate late or missing check-outs at midnight    Run every night at 12:00 AM
    @Scheduled(cron = "0 0 0 * * *")
    public void evaluateLateOrMissingCheckOuts() {
        System.out.println(" evaluateLateOrMissingCheckOuts() executed!");
        LocalDate today = LocalDate.now();
        List<Attendance> todayAttendances = attendanceRepo.findAllByAttendanceDate(today);

        for (Attendance attendance : todayAttendances) {
            TeacherSchedule schedule = attendance.getSchedule();
            if (schedule == null) continue;

            int penaltyCount = 0;
            LocalTime checkInTime = attendance.getCheckInTime() != null ? attendance.getCheckInTime().toLocalTime() : null;
            LocalDateTime checkOutTime = attendance.getCheckOutTime();
            LocalTime scheduleStart = schedule.getStartTime();
            LocalTime scheduleEnd = schedule.getEndTime();
            LocalDateTime scheduledEndTime = LocalDateTime.of(today, scheduleEnd);

            // late check-in more than 15 minutes
            if (checkInTime != null && checkInTime.isAfter(scheduleStart.plusMinutes(15))) {
                penaltyCount++;
            }

            //early check-out before 10 minutes of end
            if (checkOutTime != null && checkOutTime.isBefore(scheduledEndTime.minusMinutes(10))) {
                penaltyCount++;
            }

            // missing check-out after 1 hour of scheduled end
            if (checkInTime != null && checkOutTime == null &&
                    LocalDateTime.now().isAfter(scheduledEndTime.plusHours(1))) {
                attendance.setCheckOutTime(scheduledEndTime.plusHours(1));
                penaltyCount++;
                System.out.println("Hypothetical check-out recorded for" + attendance.getEmail());
            }

            if (penaltyCount > 0) {
                attendance.setActualCredits(Math.max(0, attendance.getExpectedCredits() - penaltyCount));
                attendance.setAbsent(true); //// Mark as partially absent
                attendanceRepo.save(attendance);

                Teachers teacher = attendance.getUser();
                teacher.setCredits(Math.max(0, teacher.getCredits() - penaltyCount));
                teachersRepo.save(teacher);
            }
        }
    }


    // Verify email and send QR code
    public String verifyEmail(String token) {
        Optional<Teachers> optionalUser = teachersRepo.findByVerificationToken(token);
        if (optionalUser.isPresent()) {
            Teachers user = optionalUser.get();
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            teachersRepo.save(user);

            try {
                byte[] qrCodeImage = qrCodeService.generateQRCode(user.getEmail());
                emailService.sendEmailWithAttachment(
                        user.getEmail(),
                        "Your Attendance QR Code",
                        "Dear " + user.getName() + ",\n\nYour email has been verified.\nPlease find your attendance QR code attached.",
                        qrCodeImage
                );
            } catch (Exception e) {
                return "Verified, but error sending QR code: " + e.getMessage();
            }

            return "Email verified successfully. QR Code sent.";
        } else {
            return "Invalid or expired verification token.";
        }
    }
    public Optional<Teachers> findByEmail(String email) {
        return teachersRepo.findByEmail(email);
    }

    public Optional<Teachers> findById(Long id) {
        return teachersRepo.findById(id);
    }
}