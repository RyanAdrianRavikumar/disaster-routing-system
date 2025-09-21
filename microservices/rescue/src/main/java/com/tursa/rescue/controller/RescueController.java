package com.tursa.rescue.controller;

import com.tursa.rescue.dto.UserDTO;
import com.tursa.rescue.service.RescueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RescueController {

    @Autowired
    private RescueService rescueService;

    @PostMapping(path = "/rescue/enqueue")
    public ResponseEntity<String> enqueueUsers() {
        String msg = rescueService.enqueueUsersFromUserService();
        return ResponseEntity.ok(msg);
    }

    @GetMapping(path = "/rescue/peek")
    public ResponseEntity<?> peekNextUser() {
        try {
            return ResponseEntity.ok(rescueService.peekNextUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Queue is empty");
        }
    }

    @PostMapping(path = "/rescue")
    public ResponseEntity<?> rescueNextUser() {
        try {
            return ResponseEntity.ok(rescueService.rescueNextUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Queue is empty");
        }
    }

    @GetMapping(path = "/rescue/size")
    public ResponseEntity<Integer> getQueueSize() {
        return ResponseEntity.ok(rescueService.queueSize());
    }

    @GetMapping(path = "rescue/isEmpty")
    public ResponseEntity<Boolean> isQueueEmpty() {
        return ResponseEntity.ok(rescueService.isQueueEmpty());
    }

    @GetMapping(path = "/rescue/isFull")
    public ResponseEntity<Boolean> isQueueFull() {
        return ResponseEntity.ok(rescueService.isQueueFull());
    }
}
