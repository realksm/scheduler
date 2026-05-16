package com.schd.scheduler.models;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Availability {

    private UUID id;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotEmpty(message = "At least one day of week is required")
    private List<@Min(1) @Max(7) Integer> daysOfWeek;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    private LocalDate dateOverride;

    private Boolean isRecurring = true;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public boolean isTimeRangeValid() {
        if (startTime == null || endTime == null) return false;
        return endTime.isAfter(startTime);
    }
}
