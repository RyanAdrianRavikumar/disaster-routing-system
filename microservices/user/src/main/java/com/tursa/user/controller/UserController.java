package com.tursa.user.controller;

import com.tursa.user.entity.User;
import com.tursa.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @GetMapping(path = "/users/all")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));

        } catch (Exception e) {
            logger.error("Error retrieving all users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @PostMapping(path = "/users/register")
    public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", createdUser));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }

    @GetMapping(path = "/users/rfid/{rfid}")
    public ResponseEntity<ApiResponse<User>> getUserByRfid(@PathVariable String rfid) {
        try {
            User user = userService.findByRfid(rfid);
            return ResponseEntity.ok(new ApiResponse<>(true, "User found", user));

        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound()
                    .build();
        } catch (Exception e) {
            logger.error("Error finding user {}: {}", rfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Internal server error", null));
        }
    }
}
