package com.schd.scheduler.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Calendar {

    private UUID id;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Integration provider is required")
    @Size(max = 50)
    private String integration;

    @NotBlank(message = "Calendar account email is required")
    @Email(message = "Calendar account email must be valid")
    @Size(max = 255)
    private String email;

    private String accessToken;
    private String refreshToken;
    private OffsetDateTime tokenExpiresAt;

    @Size(max = 255)
    private String externalCalendarId;

    private Boolean isPrimary = false;
    private Boolean isActive = true;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

}
