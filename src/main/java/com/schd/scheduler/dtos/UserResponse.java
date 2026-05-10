package com.schd.scheduler.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String timezone;
    private String locale;
    private Boolean emailVerified;
    private String oauthProvider;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}