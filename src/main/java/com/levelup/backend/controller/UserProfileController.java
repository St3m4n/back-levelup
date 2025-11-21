package com.levelup.backend.controller;

import com.levelup.backend.dto.UserProfileDto;
import com.levelup.backend.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> me() {
        return ResponseEntity.ok(userProfileService.getCurrentProfile());
    }
}
