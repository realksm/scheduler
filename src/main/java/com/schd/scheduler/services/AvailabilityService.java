package com.schd.scheduler.services;

import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.schd.scheduler.generated.tables.records.AvailabilityRulesRecord;
import com.schd.scheduler.repositories.AvailabilityRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AvailabilityService {
     private final AvailabilityRepository availabilityRepository;

    public Optional<AvailabilityRulesRecord> findById(UUID id) {
        return availabilityRepository.findById(id);
    }
    
    public List<AvailabilityRulesRecord> findByUserId(UUID userId) {
        return availabilityRepository.findByUserId(userId);
    }

    public Optional<AvailabilityRulesRecord> findByStartTime(LocalTime startTime) {
        return availabilityRepository.findByStartTime(startTime);
    }

    public Optional<AvailabilityRulesRecord> findByEndTime(LocalTime endTime) {
        return availabilityRepository.findByEndTime(endTime);
    }

    public List<AvailabilityRulesRecord> findByDaysOfWeeks(Integer dayOfWeek) {
        return availabilityRepository.findByDaysOfWeeks(dayOfWeek);
    }

    public AvailabilityRulesRecord create(AvailabilityRulesRecord record) {
        if (availabilityRepository.findById(record.getId()).isPresent()) {
            throw new IllegalArgumentException("Availbility Id is already utilized");
        }
        
        record.setCreatedAt(OffsetDateTime.now());
        record.setUpdatedAt(OffsetDateTime.now());
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        
        return availabilityRepository.create(record);
    }

    public AvailabilityRulesRecord update(AvailabilityRulesRecord record) {
        record.setUpdatedAt(OffsetDateTime.now());
        availabilityRepository.update(record);
        return availabilityRepository.findById(record.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void deleteById(UUID id) {
        availabilityRepository.deleteById(id);
    }

    public void deleteByUserId(UUID userId) {
        availabilityRepository.deleteByUserId(userId);
    }
}
