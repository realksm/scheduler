package com.schd.scheduler.services;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.schd.scheduler.repositories.UserRepository;
import com.schd.scheduler.generated.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<UsersRecord> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<UsersRecord> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<UsersRecord> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UsersRecord create(UsersRecord record) {
        if (userRepository.findByEmail(record.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }
        if (userRepository.findByUsername(record.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already in use");
        }
        
        record.setCreatedAt(OffsetDateTime.now());
        record.setUpdatedAt(OffsetDateTime.now());
        if (record.getId() == null) {
            record.setId(UUID.randomUUID());
        }
        
        return userRepository.create(record);
    }

    public UsersRecord update(UsersRecord record) {
        record.setUpdatedAt(OffsetDateTime.now());
        userRepository.update(record);
        return userRepository.findById(record.getId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }
}