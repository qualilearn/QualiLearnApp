package com.webapp.courses.config;

import com.webapp.courses.dao.CourseRepository;
import com.webapp.courses.dao.UserRepository;
import com.webapp.courses.entity.Course;
import com.webapp.courses.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class BaseConfiguration {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers("/register", "/webjars/**", "/styles.css").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .formLogin();
        return http.build();
    }

    @Bean
    public void populateUser() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "admin", passwordEncoder.encode("admin"), "ROLE_ADMIN", "Name", "Surname", "+37066666666", "https://hatrabbits.com/wp-content/uploads/2017/01/random.jpg", 45));
        users.add(new User(2, "user1", passwordEncoder.encode("user1Pass"), "ROLE_STUDENT", "Name", "Surname", "+37066666666", "https://hatrabbits.com/wp-content/uploads/2017/01/random.jpg", 45));
        users.add(new User(3, "lecture", passwordEncoder.encode("lecture"), "ROLE_LECTURER", "Name", "Surname", "+37066666666", "https://hatrabbits.com/wp-content/uploads/2017/01/random.jpg", 45));
        log.info("Populating users to database... total - {}", users.size());
        userRepository.saveAll(users);
    }

    @Bean
    public void populateCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course(1, "Math", "Some desc", "https://google.com", "https://www.surveymonkey.com/r/9778G2K", 50, false));
        courses.add(new Course(2, "Computer Science", "Some desc", "https://google.com", "https://www.surveymonkey.com/r/9778G2K", 40, false));
        courses.add(new Course(3, "Physics", "Some desc", "https://google.com", "https://www.surveymonkey.com/r/9778G2K", 23, true));
        courses.add(new Course(4, "Math 2", "Some desc", "https://google.com", "https://www.surveymonkey.com/r/9778G2K", 11, true));
        log.info("Populating courses to database... total - {}", courses.size());
        courseRepository.saveAll(courses);
    }
}