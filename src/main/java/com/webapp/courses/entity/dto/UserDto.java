package com.webapp.courses.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String username;

    private String password;

    private String role;

    private String name;

    private String surname;

    private String phone;

    private String imageUrl;

    private int age;
}
