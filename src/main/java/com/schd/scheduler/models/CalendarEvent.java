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
public class CalendarEvent {

    private UUID id;

    @NotNull(message = "Calendar ID is required")
    private UUID calendarId;

    @NotBlank(message = "External event ID is required")
    @Size(max = 255)
    private String externalEventId;

    @Size(max = 255)
    private String title;

    private String description;

    @NotNull(message = "Start time is required")
    private OffsetDateTime startTime;

    @NotNull(message = "End time is required")
    private OffsetDateTime endTime;

    @Size(max = 500)
    private String location;

    private String attendees = "[]";

    private Boolean isRecurring = false;

    @Size(max = 500)
    private String recurrenceRule;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) return false;
        return endTime.isAfter(startTime);
    }
}
