package com.schd.scheduler.repositories;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

import com.schd.scheduler.generated.tables.AvailabilityRules;
import com.schd.scheduler.generated.tables.records.AvailabilityRulesRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AvailabilityRepository {
     private final DSLContext dsl;

    public Optional<AvailabilityRulesRecord> findById(UUID id) {
        return dsl.selectFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.ID.eq(id))
            .fetchOptional();
    } 

    public List<AvailabilityRulesRecord> findByUserId(UUID userId) {
        return dsl.selectFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.USER_ID.eq(userId))
            .fetch();
    }

    public Optional<AvailabilityRulesRecord> findByStartTime(LocalTime startTime) {
        return dsl.selectFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.START_TIME.eq(startTime))
            .fetchOptional();
    }

    public Optional<AvailabilityRulesRecord> findByEndTime(LocalTime endTime) {
        return dsl.selectFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.END_TIME.eq(endTime))
            .fetchOptional();
    }

    public Optional<AvailabilityRulesRecord> findByRecurrence(Boolean isRecurr) {
        return dsl.selectFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.IS_RECURRING.eq(isRecurr))
            .fetchOptional();
    }
    
    public List<AvailabilityRulesRecord> findByDaysOfWeeks(Integer dayOfWeek) {
        return dsl.selectFrom(AvailabilityRules.AVAILABILITY_RULES)
                .where(DSL.val(dayOfWeek).eq(DSL.any(AvailabilityRules.AVAILABILITY_RULES.DAYS_OF_WEEK)))
                .fetch(); 
    }

    public AvailabilityRulesRecord create(AvailabilityRulesRecord record) {
        dsl.insertInto(AvailabilityRules.AVAILABILITY_RULES)
            .set(record)
            .execute();
        return record;
    }

    public int update(AvailabilityRulesRecord record) {
        return dsl.update(AvailabilityRules.AVAILABILITY_RULES)
            .set(record)
            .where(AvailabilityRules.AVAILABILITY_RULES.ID.eq(record.getId()))
            .execute();
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.ID.eq(id))
            .execute();
    }

    public int deleteByUserId(UUID userId) {
        return dsl.deleteFrom(AvailabilityRules.AVAILABILITY_RULES)
            .where(AvailabilityRules.AVAILABILITY_RULES.USER_ID.eq(userId))
            .execute();
    }
}
