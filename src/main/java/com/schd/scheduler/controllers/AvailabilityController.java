package com.schd.scheduler.controllers;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.schd.scheduler.dtos.AvailabilityRequestDto;
import com.schd.scheduler.dtos.AvailabilityResponseDto;
import com.schd.scheduler.generated.tables.records.AvailabilityRulesRecord;
import com.schd.scheduler.services.AvailabilityService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDto>> getMyRules() {
        UUID userId = getCurrentUserId();
        List<AvailabilityRulesRecord> records = availabilityService.findByUserId(userId);
        return ResponseEntity.ok(records.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> getById(@PathVariable UUID id) {
        return availabilityService.findById(id)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-day/{dayOfWeek}")
    public ResponseEntity<List<AvailabilityResponseDto>> getByDayOfWeek(@PathVariable Integer dayOfWeek) {
        List<AvailabilityRulesRecord> records = availabilityService.findByDaysOfWeeks(dayOfWeek);
        return ResponseEntity.ok(records.stream().map(this::toResponse).toList());
    }

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> create(@Valid @RequestBody AvailabilityRequestDto request) {
        AvailabilityRulesRecord record = new AvailabilityRulesRecord();
        record.setUserId(getCurrentUserId());
        record.setDaysOfWeek(request.daysOfWeek.toArray(new Integer[0]));
        record.setStartTime(request.startTime);
        record.setEndTime(request.endTime);
        record.setDateoverride(request.dateOverride);
        record.setIsRecurring(request.isRecurring != null ? request.isRecurring : true);

        AvailabilityRulesRecord created = availabilityService.create(record);
        return ResponseEntity.created(URI.create("/api/availability/" + created.getId()))
            .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AvailabilityResponseDto> update(@PathVariable UUID id, @Valid @RequestBody AvailabilityRequestDto request) {
        return availabilityService.findById(id)
            .map(existing -> {
                if (request.daysOfWeek != null) existing.setDaysOfWeek(request.daysOfWeek.toArray(new Integer[0]));
                if (request.startTime != null) existing.setStartTime(request.startTime);
                if (request.endTime != null) existing.setEndTime(request.endTime);
                if (request.dateOverride != null) existing.setDateoverride(request.dateOverride);
                if (request.isRecurring != null) existing.setIsRecurring(request.isRecurring);

                AvailabilityRulesRecord updated = availabilityService.update(existing);
                return toResponse(updated);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (availabilityService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        availabilityService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) auth.getPrincipal();
    }

    private AvailabilityResponseDto toResponse(AvailabilityRulesRecord record) {
        AvailabilityResponseDto dto = new AvailabilityResponseDto();
        dto.id = record.getId();
        dto.userId = record.getUserId();
        dto.daysOfWeek = record.getDaysOfWeek() != null
            ? Arrays.asList(record.getDaysOfWeek())
            : List.of();
        dto.startTime = record.getStartTime();
        dto.endTime = record.getEndTime();
        dto.dateOverride = record.getDateoverride();
        dto.isRecurring = record.getIsRecurring();
        dto.createdAt = record.getCreatedAt();
        dto.updatedAt = record.getUpdatedAt();
        return dto;
    }
}
