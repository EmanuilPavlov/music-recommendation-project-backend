package com.example.student_system.musicproject.controllers;

import com.example.student_system.musicproject.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/search-history/{uid}")
    public ResponseEntity<?> getUserSearchHistory(@PathVariable String uid) {
        return ResponseEntity.ok(userService.getSearchHistory(uid));
    }
}
