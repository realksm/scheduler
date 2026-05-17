package com.schd.scheduler.repositories;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.schd.scheduler.generated.tables.CalendarEvents;
import com.schd.scheduler.generated.tables.records.CalendarEventsRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CalendarEventRepository {
     private final DSLContext dsl;

    public Optional<CalendarEventsRecord> findById(UUID id) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.ID.eq(id))
            .fetchOptional();
    }

    public List<CalendarEventsRecord> findByCalendarId(UUID calendarId) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.CALENDAR_ID.eq(calendarId))
            .fetch();
    }

    public Optional<CalendarEventsRecord> findByExternalEventId(String externalEventId) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.EXTERNAL_EVENT_ID.eq(externalEventId))
            .fetchOptional();
    }

    public List<CalendarEventsRecord> findByTitle(String title) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.TITLE.eq(title))
            .fetch();
    }

    public List<CalendarEventsRecord> findByDescription(String description) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.DESCRIPTION.eq(description))
            .fetch();
    }

    public List<CalendarEventsRecord> findByCalendarIdAndTimeRange(UUID calendarId, OffsetDateTime start, OffsetDateTime end) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.CALENDAR_ID.eq(calendarId))
            .and(CalendarEvents.CALENDAR_EVENTS.START_TIME.lessThan(end))
            .and(CalendarEvents.CALENDAR_EVENTS.END_TIME.greaterThan(start))
            .fetch();
    }

    public List<CalendarEventsRecord> findByStartTimeBetween(OffsetDateTime from, OffsetDateTime to) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.START_TIME.between(from, to))
            .fetch();
    }

    public List<CalendarEventsRecord> findUpcomingByCalendarId(UUID calendarId, OffsetDateTime from) {
        return dsl.selectFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.CALENDAR_ID.eq(calendarId))
            .and(CalendarEvents.CALENDAR_EVENTS.START_TIME.greaterOrEqual(from))
            .orderBy(CalendarEvents.CALENDAR_EVENTS.START_TIME)
            .fetch();
    }

    public CalendarEventsRecord create(CalendarEventsRecord record) {
        dsl.insertInto(CalendarEvents.CALENDAR_EVENTS)
            .set(record)
            .execute();
        return record;
    }

    public int update(CalendarEventsRecord record) {
        return dsl.update(CalendarEvents.CALENDAR_EVENTS)
            .set(record)
            .where(CalendarEvents.CALENDAR_EVENTS.ID.eq(record.getId()))
            .execute();
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.ID.eq(id))
            .execute();
    }

    public int deleteByCalendarId(UUID calendarId) {
        return dsl.deleteFrom(CalendarEvents.CALENDAR_EVENTS)
            .where(CalendarEvents.CALENDAR_EVENTS.CALENDAR_ID.eq(calendarId))
            .execute();
    }
}
