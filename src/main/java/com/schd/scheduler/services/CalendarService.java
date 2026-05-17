package com.schd.scheduler.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.schd.scheduler.generated.tables.records.CalendarsRecord;
import com.schd.scheduler.repositories.CalendarRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final CalendarRepository calendarRepository;

    public Optional<CalendarsRecord> findById(UUID id) {
        return calendarRepository.findById(id);
    }

    public List<CalendarsRecord> findByUserId(UUID userId) {
        return calendarRepository.findByUserId(userId);
    }

    public List<CalendarsRecord> findByEmail(String email) {
        return calendarRepository.findByEmail(email);
    }

    public Optional<CalendarsRecord> findByExternalCalendarId(String externalCalendarID) {
        return calendarRepository.findByExternalCalendarId(externalCalendarID);
    }

    public CalendarsRecord create(CalendarsRecord record) {
        if (!calendarRepository.findByEmail(record.getEmail()).isEmpty()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (!calendarRepository.findByUserId(record.getUserId()).isEmpty()) {
            throw new IllegalArgumentException("User already has calendars");
        }

        record.setCreatedAt(OffsetDateTime.now());
        record.setUpdatedAt(OffsetDateTime.now());
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }

        return calendarRepository.create(record);
    }

    public CalendarsRecord update(CalendarsRecord record) {
        record.setUpdatedAt(OffsetDateTime.now());
        calendarRepository.update(record);
        return calendarRepository.findById(record.getId())
            .orElseThrow(() -> new IllegalArgumentException("Calendar not found"));
    }

    public void deleteById(UUID id) {
        calendarRepository.deleteById(id);
    }

    public void deleteByUserId(UUID userId) {
        calendarRepository.deleteByUserId(userId);
    }

    public void deleteByEmail(String email) {
        calendarRepository.deleteByEmail(email);
    }
}
