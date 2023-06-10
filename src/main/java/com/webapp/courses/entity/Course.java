package com.webapp.courses.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Course {

    @Id
    private long id;

    private String title;

    private String description;

    private String photoUrl;

    private String knowledgeTestUrl;

    private int hours;

    private boolean paid;
}
