package com.webapp.courses.dao;

import com.webapp.courses.entity.Enrollment;
import com.webapp.courses.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findAllByUsername(String username);
    List<Enrollment> findAllByUsernameAndEnrollmentStatus(String username, EnrollmentStatus enrollmentStatus);

    Enrollment findByCourseIdAndUsername(long courseId, String username);
}
