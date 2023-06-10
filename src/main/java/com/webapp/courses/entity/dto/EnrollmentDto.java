package com.webapp.courses.entity.dto;

import com.webapp.courses.entity.EnrollmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentDto {
    private Long courseId;
    private EnrollmentStatus enrollmentStatus;
}
