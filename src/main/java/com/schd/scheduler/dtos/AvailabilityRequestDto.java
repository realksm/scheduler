package com.schd.scheduler.dtos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class AvailabilityRequestDto {

    @NotEmpty(message = "At least one day of week is required")
    public List<@Min(1) @Max(7) Integer> daysOfWeek;

    @NotNull(message = "Start time is required")
    public LocalTime startTime;

    @NotNull(message = "End time is required")
    public LocalTime endTime;

    public LocalDate dateOverride;

    public Boolean isRecurring = true;
}
