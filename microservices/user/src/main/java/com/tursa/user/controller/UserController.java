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
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving all users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping(path = "/users/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.ok(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(path = "/users/rfid/{rfid}")
    public ResponseEntity<User> getUserByRfid(@PathVariable String rfid) {
        try {
            User user = userService.findByRfid(rfid);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error finding user {}: {}", rfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(path = "/users/location/{rfid}")
    public ResponseEntity<User> updateLocation(@PathVariable String rfid, @RequestParam Double latitude, @RequestParam Double longitude) {
        try {
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return ResponseEntity.badRequest().build();
            }

            User user = userService.updateLocation(rfid, latitude, longitude);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating location for user {}: {}", rfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(path = "/users/status/{rfid}")
    public ResponseEntity<User> updateStatus(@PathVariable String rfid, @RequestParam User.UserStatus status) {
        try {
            User user = userService.updateUserStatus(rfid, status);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating status for user {}: {}", rfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(path = "/users/rescue")
    public ResponseEntity<List<User>> getUsersNeedingRescue() {
        try {
            List<User> users = userService.getUsersNeedingRescue();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users needing rescue: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping(path = "/users/area")
    public ResponseEntity<List<User>> getUsersInArea(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(defaultValue = "5.0") Double radiusKm) {
        try {
            List<User> users = userService.getUsersInArea(latitude, longitude, radiusKm);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            logger.error("Error retrieving users in area: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping(path = "/users/family/{rfid}")
    public ResponseEntity<User> updateFamilyInfo(@PathVariable String rfid, @RequestBody User request) {
        try {
            User user = userService.updateFamilyInfo(rfid,
                    request.getFamilyCount(),
                    request.getChildrenCount(),
                    request.getElderlyCount());
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error updating family info for user {}: {}", rfid, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
