package com.schd.scheduler.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private UUID id;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @Size(max = 255)
    private String passwordHash;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-z0-9_-]+$", message = "Username may only contain lowercase letters, numbers, hyphens and underscores")
    private String username;

    @NotBlank(message = "Full name is required")
    @Size(max = 255)
    private String fullName;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 100)
    private String timezone = "UTC";

    @Size(max = 10)
    private String locale = "en";

    @NotNull
    private Boolean emailVerified = false;

    @Size(max = 50)
    private String oauthProvider;

    @Size(max = 255)
    private String oauthProviderId;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}