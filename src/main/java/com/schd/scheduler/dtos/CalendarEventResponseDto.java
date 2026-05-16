package com.schd.scheduler.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class CalendarEventResponseDto {
    public UUID id;
    public UUID calendarId;
    public String externalEventId;
    public String title;
    public String description;
    public OffsetDateTime startTime;
    public OffsetDateTime endTime;
    public String location;
    public String attendees;
    public Boolean isRecurring;
    public String recurrenceRule;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;
}
