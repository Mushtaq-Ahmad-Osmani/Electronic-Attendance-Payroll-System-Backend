package com.electronicattendancesystem.Electronic_attendance.repository;

import com.electronicattendancesystem.Electronic_attendance.entity.Teachers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeachersRepo extends JpaRepository<Teachers,Long> {
    Optional<Teachers> findByEmail(String email);
    Optional<Teachers> findByVerificationToken(String token);

    List<Teachers> findAllByIsTeacherTrue();

    Optional<Teachers> findByPhone(String phone);
}
