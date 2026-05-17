package com.schd.scheduler.dtos;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CalendarEventRequestDto {

    @NotNull(message = "Calendar ID is required")
    public String calendarId;

    @NotBlank(message = "External event ID is required")
    @Size(max = 255)
    public String externalEventId;

    @Size(max = 255)
    public String title;

    public String description;

    @NotNull(message = "Start time is required")
    public OffsetDateTime startTime;

    @NotNull(message = "End time is required")
    public OffsetDateTime endTime;

    @Size(max = 500)
    public String location;

    public String attendees = "[]";

    public Boolean isRecurring = false;

    @Size(max = 500)
    public String recurrenceRule;
}
