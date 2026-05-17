package com.schd.scheduler.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.schd.scheduler.generated.tables.Calendars;
import com.schd.scheduler.generated.tables.records.CalendarsRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CalendarRepository {
     private final DSLContext dsl;

    public Optional<CalendarsRecord> findById(UUID id) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.ID.eq(id))
            .fetchOptional();
    }

    public List<CalendarsRecord> findByUserId(UUID userId) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.USER_ID.eq(userId))
            .fetch();
    }

    public List<CalendarsRecord> findByEmail(String email) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.EMAIL.eq(email))
            .fetch();
    }

    public Optional<CalendarsRecord> findByExternalCalendarId(String extCalenderId) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.EXTERNAL_CALENDAR_ID.eq(extCalenderId))
            .fetchOptional();
    }

    public List<CalendarsRecord> findByIsActive(Boolean isActive) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.IS_ACTIVE.eq(isActive))
            .fetch();
    }

    public List<CalendarsRecord> findByIntegration(String integration) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.INTEGRATION.eq(integration))
            .fetch();
    }

    public Optional<CalendarsRecord> findByUserIdAndIntegration(UUID userId, String integration) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.USER_ID.eq(userId))
            .and(Calendars.CALENDARS.INTEGRATION.eq(integration))
            .fetchOptional();
    }

    public Optional<CalendarsRecord> findPrimaryByUserId(UUID userId) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.USER_ID.eq(userId))
            .and(Calendars.CALENDARS.IS_PRIMARY.eq(true))
            .fetchOptional();
    }

    public List<CalendarsRecord> findByTokenExpiresAtBefore(OffsetDateTime time) {
        return dsl.selectFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.TOKEN_EXPIRES_AT.lessOrEqual(time))
            .fetch();
    }

    public List<CalendarsRecord> findAll() {
        return dsl.selectFrom(Calendars.CALENDARS)
            .fetch();
    }

    public CalendarsRecord create(CalendarsRecord record) {
        dsl.insertInto(Calendars.CALENDARS)
            .set(record)
            .execute();
        return record;
    }

    public int update(CalendarsRecord record) {
        return dsl.update(Calendars.CALENDARS)
            .set(record)
            .where(Calendars.CALENDARS.ID.eq(record.getId()))
            .execute();
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.ID.eq(id))
            .execute();
    }

    public int deleteByUserId(UUID userId) {
        return dsl.deleteFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.USER_ID.eq(userId))
            .execute();
    }

     public int deleteByEmail(String email) {
        return dsl.deleteFrom(Calendars.CALENDARS)
            .where(Calendars.CALENDARS.EMAIL.eq(email))
            .execute();
    }
}
