package com.schd.scheduler.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class AvailabilityResponseDto {
    public UUID id;
    public UUID userId;
    public List<Integer> daysOfWeek;
    public LocalTime startTime;
    public LocalTime endTime;
    public LocalDate dateOverride;
    public Boolean isRecurring;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

}
