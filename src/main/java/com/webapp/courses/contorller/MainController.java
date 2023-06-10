package com.webapp.courses.contorller;

import com.lowagie.text.DocumentException;
import com.webapp.courses.dao.CourseRepository;
import com.webapp.courses.dao.DepositRepository;
import com.webapp.courses.dao.EnrollmentRepository;
import com.webapp.courses.dao.UserRepository;
import com.webapp.courses.entity.*;
import com.webapp.courses.entity.dto.DeleteDepositDto;
import com.webapp.courses.entity.dto.EnrollmentDto;
import com.webapp.courses.entity.dto.UserDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {

    private final DepositRepository depositRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String hello() {
        return "redirect:/courses";
    }

    @GetMapping("/deposits")
    public String getDeposits(Model model, Authentication authentication) {
        List<Deposit> deposits = depositRepository.findAll();
        List<Deposit> depositsForCurrentUser = depositRepository.findAllByUsername(authentication.getName());
        model.addAttribute("deposits", deposits);
        model.addAttribute("userDeposits", depositsForCurrentUser);
        model.addAttribute("deleteDepositDto", new DeleteDepositDto());

        return "deposits";
    }

    @PostMapping("/deposit")
    public String createDeposit(Model model, Authentication authentication) {
        Deposit deposit = new Deposit();
        deposit.setUsername(authentication.getName());
        depositRepository.save(deposit);

        List<Deposit> deposits = depositRepository.findAll();
        model.addAttribute("deposits", deposits);
        log.info("Saving a Deposit - {}", deposit);
        return "redirect:/deposits";
    }

    @PostMapping("/deleteDeposit")
    public String deleteDeposit(@ModelAttribute DeleteDepositDto deleteDepositDto, Model model) {
        log.info("Deleting deposit with id - {}", deleteDepositDto.getId());
        List<Deposit> deposits = depositRepository.findAll();
        model.addAttribute("deposits", deposits);
        depositRepository.deleteById(deleteDepositDto.getId());
        return "redirect:/deposits";
    }

    @GetMapping("/courses")
    public String showCourses(Model model, Authentication authentication) {
        List<Enrollment> enrollments = enrollmentRepository.findAllByUsername(authentication.getName());
        List<Course> courses = courseRepository.findAll();
        Map<Long, Enrollment> courseIdEnrollments = new HashMap<>();
        enrollments.stream()
                .forEach(enrollment -> courseIdEnrollments.put(enrollment.getCourseId(), enrollment));
        List<Deposit> userDeposits = depositRepository.findAllByUsername(authentication.getName());

        model.addAttribute("userPaid", userDeposits.size() > 0);
        model.addAttribute("courses", courses);
        model.addAttribute("courseIdEnrollments", courseIdEnrollments);
        model.addAttribute("enrollmentDto", new EnrollmentDto());
        return "courses";
    }

    @PostMapping("/enrollment")
    public String enrollment(@ModelAttribute EnrollmentDto enrollmentDto, Model model, Authentication authentication) {
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(enrollmentDto.getCourseId());
        enrollment.setUsername(authentication.getName());
        enrollment.setEnrollmentStatus(enrollmentDto.getEnrollmentStatus());
        enrollmentRepository.save(enrollment);
        log.info("Enrollment saved - {}", enrollment);
        return "redirect:/courses";
    }

    @PostMapping("/complete")
    public String complete(@ModelAttribute EnrollmentDto enrollmentDto, Model model, Authentication authentication) {
        Enrollment enrollment = enrollmentRepository.findByCourseIdAndUsername(enrollmentDto.getCourseId(), authentication.getName());
        enrollment.setEnrollmentStatus(enrollmentDto.getEnrollmentStatus());
        enrollmentRepository.save(enrollment);
        log.info("Enrollment updated - {}", enrollment);
        return "redirect:/courses";
    }

    @GetMapping("/certificate")
    public String certificate(Model model, Authentication authentication) {
        List<Enrollment> completeEnrollments = enrollmentRepository.findAllByUsernameAndEnrollmentStatus(authentication.getName(), EnrollmentStatus.COMPLETED);
        List<Course> allCourses = courseRepository.findAll();
        HashMap<Long, Course> courses = new HashMap<>();
        for(var course : allCourses) {
            courses.put(course.getId(), course);
        }

        model.addAttribute("completeEnrollments", completeEnrollments);
        model.addAttribute("courses", courses);
        return "certificate";
    }

    @GetMapping("/download/pdf/{courseId}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long courseId) throws IOException, DocumentException {
        Course course = courseRepository.findById(courseId).get();
        generatePdfFromHtml("<h2>You finished course " + course.getTitle() + "</h2>");
        // Load the PDF file as a resource (replace "path/to/pdf/file.pdf" with the actual path to your PDF file)
        InputStream inputStream = new FileInputStream(System.getProperty("user.home") + File.separator + "thymeleaf.pdf");
        Resource resource = new InputStreamResource(inputStream);

        // Set the response headers
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=file.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/match")
    public String match(Model model) {
        List<User> lectures = userRepository.findAllByRole("ROLE_LECTURER");
        int randomMatch = new Random().nextInt(lectures.size());
        model.addAttribute("lecturer", lectures.get(randomMatch));
        return "match";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("userDto", new UserDto());
        return "registration";
    }

    @PostMapping("/register")
    public String processRegistrationForm(@Valid @ModelAttribute("user") UserDto userDto, BindingResult result) {
        if (result.hasErrors()) {
            return "registration";
        }

        // Check if the username is already taken
        if (userRepository.findByUsername(userDto.getUsername()) != null) {
            result.rejectValue("username", null, "Username already exists");
            return "registration";
        }

        // Map fields to the entity

        User user = new User();

        user.setUsername(userDto.getUsername());
        user.setImageUrl(userDto.getImageUrl());
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setAge(userDto.getAge());
        user.setPhone(userDto.getPhone());

        if (userDto.getRole().equals("Student")) {
            user.setRole("ROLE_STUDENT");
        } else if (userDto.getRole().equals("Lecturer")) {
            user.setRole("ROLE_LECTURER");
        }
        // Encode the password before saving the user
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Save the user to the database
        userRepository.save(user);

        // Redirect to the login page or any other desired page
        return "redirect:/login";
    }

    @GetMapping("/course")
    public String course() {
        return "course";
    }

    public void generatePdfFromHtml(String html) throws IOException, DocumentException {
        String outputFolder = System.getProperty("user.home") + File.separator + "thymeleaf.pdf";
        OutputStream outputStream = new FileOutputStream(outputFolder);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        outputStream.close();
    }
}
