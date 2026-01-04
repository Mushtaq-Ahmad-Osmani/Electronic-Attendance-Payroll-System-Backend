package com.electronicattendancesystem.Electronic_attendance.controller;

import com.electronicattendancesystem.Electronic_attendance.dto.ReqRes;
import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import com.electronicattendancesystem.Electronic_attendance.repository.TeachersRepo;
import com.electronicattendancesystem.Electronic_attendance.service.UsersManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;



@RestController
public class UserManagementController {

    @Autowired
    private UsersManagementService usersManagementService;

    @Autowired
    private TeachersRepo teachersRepo;

    @PostMapping("/auth/register")
    public ResponseEntity<ReqRes> register(@RequestBody ReqRes reg) {
        ReqRes response = usersManagementService.register(reg);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @PostMapping("/auth/login")
    public ResponseEntity<ReqRes>login(@RequestBody ReqRes req){
        return ResponseEntity.ok(usersManagementService.login(req));
    }
    @PostMapping("/auth/refresh")
    public ResponseEntity<ReqRes>refreshToken(@RequestBody ReqRes reg){
        return ResponseEntity.ok(usersManagementService.refreshToken(reg));
    }
        @GetMapping("/admin/get-all-users")
    public ResponseEntity<ReqRes>getAllUsers(){
        return ResponseEntity.ok(usersManagementService.getAllUsers());
    }
    @GetMapping("/admin/get-users/{userId}")
    public ResponseEntity<ReqRes> getUsersByID(@PathVariable Long userId){
        return ResponseEntity.ok(usersManagementService.getUserById(userId));
    }
    @PutMapping("/admin/update/{userId}")
    public ResponseEntity<ReqRes> updateUser(@PathVariable Long userId,  @RequestBody Teachers reqres ){
        return ResponseEntity.ok(usersManagementService.updateUser(userId ,reqres));
    }

    @PutMapping("/user/update-password")
    public ResponseEntity<ReqRes> updateUserPassword(@RequestBody ReqRes reqRes) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(usersManagementService.updateUserPassword(email, reqRes));
    }

    @GetMapping("/adminuser/get-profile")
    public ResponseEntity<ReqRes> getMyProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        ReqRes response = usersManagementService.getMyInfo(email);
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @DeleteMapping("/admin/delete/{userId}")
    public ResponseEntity<ReqRes>deleteUSer(@PathVariable Long userId){
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }

    @PostMapping("/upload-profile-picture/{userId}")
    public ResponseEntity<ReqRes> uploadProfilePicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file) {
        ReqRes response = usersManagementService.uploadProfilePicture(userId, file);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @GetMapping("/profile-picture/{userId}")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long userId) {
        try {
            Teachers user = teachersRepo.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            String imagePath = user.getProfilePicture();
            if (imagePath == null || imagePath.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Path path = Paths.get("C:/Project/profile-picture" + imagePath.replace("/uploads/", ""));
            byte[] imageBytes = Files.readAllBytes(path);
            String contentType = "image/jpeg";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/auth/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        String result = usersManagementService.verifyEmail(token);

        if (result.startsWith("Email verified")) {
            return ResponseEntity.ok(result);
        } else if (result.startsWith("Verified, but error")) {
            return ResponseEntity.status(500).body(result);
        } else {
            return ResponseEntity.status(404).body(result);
        }
    }

    @GetMapping("/api/users/info")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        Optional<Teachers> user = usersManagementService.findByEmail(email);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }
    }
}
