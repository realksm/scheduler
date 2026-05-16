package com.schd.scheduler.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CalendarRequestDto {

    @NotBlank(message = "Integration provider is required")
    @Size(max = 50)
    public String integration;

    @NotBlank(message = "Calendar account email is required")
    @Email
    @Size(max = 255)
    public String email;

    public String accessToken;

    public String refreshToken;

    @Size(max = 255)
    public String externalCalendarId;

    public Boolean isPrimary = false;

    public Boolean isActive = true;
}
