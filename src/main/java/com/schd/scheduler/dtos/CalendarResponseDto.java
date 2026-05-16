package com.schd.scheduler.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class CalendarResponseDto {
    public UUID id;
    public UUID userId;
    public String integration;
    public String email;
    public String externalCalendarId;
    public Boolean isPrimary;
    public Boolean isActive;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
