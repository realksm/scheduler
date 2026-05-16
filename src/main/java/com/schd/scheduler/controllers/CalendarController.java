package com.schd.scheduler.controllers;

import java.net.URI;
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

import com.schd.scheduler.dtos.CalendarRequestDto;
import com.schd.scheduler.dtos.CalendarResponseDto;
import com.schd.scheduler.generated.tables.records.CalendarsRecord;
import com.schd.scheduler.services.CalendarService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendars")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    public ResponseEntity<List<CalendarResponseDto>> getMyCalendars() {
        UUID userId = getCurrentUserId();
        List<CalendarsRecord> records = calendarService.findByUserId(userId);
        return ResponseEntity.ok(records.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarResponseDto> getById(@PathVariable UUID id) {
        return calendarService.findById(id)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CalendarResponseDto> create(@Valid @RequestBody CalendarRequestDto request) {
        CalendarsRecord record = new CalendarsRecord();
        record.setUserId(getCurrentUserId());
        record.setIntegration(request.integration);
        record.setEmail(request.email);
        record.setAccessToken(request.accessToken);
        record.setRefreshToken(request.refreshToken);
        record.setExternalCalendarId(request.externalCalendarId);
        record.setIsPrimary(request.isPrimary != null ? request.isPrimary : false);
        record.setIsActive(request.isActive != null ? request.isActive : true);

        CalendarsRecord created = calendarService.create(record);
        return ResponseEntity.created(URI.create("/api/calendars/" + created.getId()))
            .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CalendarResponseDto> update(@PathVariable UUID id, @Valid @RequestBody CalendarRequestDto request) {
        return calendarService.findById(id)
            .map(existing -> {
                if (request.integration != null) existing.setIntegration(request.integration);
                if (request.email != null) existing.setEmail(request.email);
                if (request.accessToken != null) existing.setAccessToken(request.accessToken);
                if (request.refreshToken != null) existing.setRefreshToken(request.refreshToken);
                if (request.externalCalendarId != null) existing.setExternalCalendarId(request.externalCalendarId);
                if (request.isPrimary != null) existing.setIsPrimary(request.isPrimary);
                if (request.isActive != null) existing.setIsActive(request.isActive);

                CalendarsRecord updated = calendarService.update(existing);
                return toResponse(updated);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (calendarService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        calendarService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (UUID) auth.getPrincipal();
    }

    private CalendarResponseDto toResponse(CalendarsRecord record) {
        CalendarResponseDto dto = new CalendarResponseDto();
        dto.id = record.getId();
        dto.userId = record.getUserId();
        dto.integration = record.getIntegration();
        dto.email = record.getEmail();
        dto.externalCalendarId = record.getExternalCalendarId();
        dto.isPrimary = record.getIsPrimary();
        dto.isActive = record.getIsActive();
        dto.createdAt = record.getCreatedAt();
        dto.updatedAt = record.getUpdatedAt();
        return dto;
    }
}
