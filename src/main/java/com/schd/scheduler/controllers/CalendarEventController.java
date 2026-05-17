package com.schd.scheduler.controllers;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schd.scheduler.dtos.CalendarEventRequestDto;
import com.schd.scheduler.dtos.CalendarEventResponseDto;
import com.schd.scheduler.generated.tables.records.CalendarEventsRecord;
import com.schd.scheduler.services.CalendarEventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendars/{calendarId}/events")
public class CalendarEventController {

    private final CalendarEventService calendarEventService;

    @GetMapping("/me")
    public ResponseEntity<List<CalendarEventResponseDto>> getEvents(
            @PathVariable UUID calendarId,
            @RequestParam(required = false) OffsetDateTime start,
            @RequestParam(required = false) OffsetDateTime end) {
        List<CalendarEventsRecord> records;
        if (start != null && end != null) {
            records = calendarEventService.findByCalendarIdAndTimeRange(calendarId, start, end);
        } else {
            records = calendarEventService.findByCalendarId(calendarId);
        }
        return ResponseEntity.ok(records.stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalendarEventResponseDto> getById(@PathVariable UUID calendarId, @PathVariable UUID id) {
        return calendarEventService.findById(id)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<CalendarEventResponseDto>> getUpcoming(
            @PathVariable UUID calendarId,
            @RequestParam(defaultValue = "#{T(java.time.OffsetDateTime).now()}") OffsetDateTime from) {
        List<CalendarEventsRecord> records = calendarEventService.findUpcomingByCalendarId(calendarId, from);
        return ResponseEntity.ok(records.stream().map(this::toResponse).toList());
    }

    @PostMapping("/me")
    public ResponseEntity<CalendarEventResponseDto> create(
            @PathVariable UUID calendarId,
            @Valid @RequestBody CalendarEventRequestDto request) {
        CalendarEventsRecord record = new CalendarEventsRecord();
        record.setCalendarId(calendarId);
        record.setExternalEventId(request.externalEventId);
        record.setTitle(request.title);
        record.setDescription(request.description);
        record.setStartTime(request.startTime);
        record.setEndTime(request.endTime);
        record.setLocation(request.location);
        record.setIsRecurring(request.isRecurring != null ? request.isRecurring : false);
        record.setRecurrenceRule(request.recurrenceRule);

        CalendarEventsRecord created = calendarEventService.create(record);
        return ResponseEntity.created(URI.create("/api/calendars/" + calendarId + "/events/" + created.getId()))
            .body(toResponse(created));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CalendarEventResponseDto> update(
            @PathVariable UUID calendarId,
            @PathVariable UUID id,
            @Valid @RequestBody CalendarEventRequestDto request) {
        return calendarEventService.findById(id)
            .map(existing -> {
                if (request.externalEventId != null) existing.setExternalEventId(request.externalEventId);
                if (request.title != null) existing.setTitle(request.title);
                if (request.description != null) existing.setDescription(request.description);
                if (request.startTime != null) existing.setStartTime(request.startTime);
                if (request.endTime != null) existing.setEndTime(request.endTime);
                if (request.location != null) existing.setLocation(request.location);
                if (request.isRecurring != null) existing.setIsRecurring(request.isRecurring);
                if (request.recurrenceRule != null) existing.setRecurrenceRule(request.recurrenceRule);

                CalendarEventsRecord updated = calendarEventService.update(existing);
                return toResponse(updated);
            })
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID calendarId, @PathVariable UUID id) {
        if (calendarEventService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        calendarEventService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private CalendarEventResponseDto toResponse(CalendarEventsRecord record) {
        CalendarEventResponseDto dto = new CalendarEventResponseDto();
        dto.id = record.getId();
        dto.calendarId = record.getCalendarId();
        dto.externalEventId = record.getExternalEventId();
        dto.title = record.getTitle();
        dto.description = record.getDescription();
        dto.startTime = record.getStartTime();
        dto.endTime = record.getEndTime();
        dto.location = record.getLocation();
        dto.attendees = record.getAttendees() != null ? record.getAttendees().data() : null;
        dto.isRecurring = record.getIsRecurring();
        dto.recurrenceRule = record.getRecurrenceRule();
        dto.createdAt = record.getCreatedAt();
        dto.updatedAt = record.getUpdatedAt();
        return dto;
    }
}
