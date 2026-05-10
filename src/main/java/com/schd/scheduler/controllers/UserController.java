package com.schd.scheduler.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schd.scheduler.dtos.UserRequest;
import com.schd.scheduler.dtos.UserResponse;
import com.schd.scheduler.generated.tables.records.UsersRecord;
import com.schd.scheduler.services.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UUID userId = getCurrentUserId();
        
        return userService.findById(userId)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@RequestBody UserRequest request) {
        UUID userId = getCurrentUserId();
        
        return userService.findById(userId)
            .map(existing -> {
                if (request.getFullName() != null) existing.setFullName(request.getFullName());
                if (request.getAvatarUrl() != null) existing.setAvatarUrl(request.getAvatarUrl());
                if (request.getTimezone() != null) existing.setTimezone(request.getTimezone());
                if (request.getLocale() != null) existing.setLocale(request.getLocale());
                
                UsersRecord updated = userService.update(existing);
                return toResponse(updated);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) auth.getPrincipal();
    }

    private UserResponse toResponse(UsersRecord record) {
        return new UserResponse(
            record.getId(),
            record.getEmail(),
            record.getUsername(),
            record.getFullName(),
            record.getAvatarUrl(),
            record.getTimezone(),
            record.getLocale(),
            record.getEmailVerified(),
            record.getOauthProvider(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}