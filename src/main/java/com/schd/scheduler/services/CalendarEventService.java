package com.schd.scheduler.services;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.schd.scheduler.generated.tables.records.CalendarEventsRecord;
import com.schd.scheduler.repositories.CalendarEventRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarEventService {

    private final CalendarEventRepository calendarEventRepository;

    public Optional<CalendarEventsRecord> findById(UUID id) {
        return calendarEventRepository.findById(id);
    }

    public List<CalendarEventsRecord> findByCalendarId(UUID calendarId) {
        return calendarEventRepository.findByCalendarId(calendarId);
    }

    public Optional<CalendarEventsRecord> findByExternalEventId(String externalEventId) {
        return calendarEventRepository.findByExternalEventId(externalEventId);
    }

    public List<CalendarEventsRecord> findByTitle(String title) {
        return calendarEventRepository.findByTitle(title);
    }

    public List<CalendarEventsRecord> findByCalendarIdAndTimeRange(UUID calendarId, OffsetDateTime start, OffsetDateTime end) {
        return calendarEventRepository.findByCalendarIdAndTimeRange(calendarId, start, end);
    }

    public List<CalendarEventsRecord> findUpcomingByCalendarId(UUID calendarId, OffsetDateTime from) {
        return calendarEventRepository.findUpcomingByCalendarId(calendarId, from);
    }

    public CalendarEventsRecord create(CalendarEventsRecord record) {
        record.setCreatedAt(OffsetDateTime.now());
        record.setUpdatedAt(OffsetDateTime.now());
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        return calendarEventRepository.create(record);
    }

    public CalendarEventsRecord update(CalendarEventsRecord record) {
        record.setUpdatedAt(OffsetDateTime.now());
        calendarEventRepository.update(record);
        return calendarEventRepository.findById(record.getId())
            .orElseThrow(() -> new IllegalArgumentException("Calendar event not found"));
    }

    public void deleteById(UUID id) {
        calendarEventRepository.deleteById(id);
    }

    public void deleteByCalendarId(UUID calendarId) {
        calendarEventRepository.deleteByCalendarId(calendarId);
    }
}
