package com.schd.scheduler.repositories;

import java.util.Optional;
import java.util.UUID;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.schd.scheduler.generated.tables.Users;
import com.schd.scheduler.generated.tables.records.UsersRecord;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final DSLContext dsl;

    public Optional<UsersRecord> findById(UUID id) {
        return dsl.selectFrom(Users.USERS)
            .where(Users.USERS.ID.eq(id))
            .fetchOptional();
    }

    public Optional<UsersRecord> findByEmail(String email) {
        return dsl.selectFrom(Users.USERS)
            .where(Users.USERS.EMAIL.eq(email))
            .fetchOptional();
    }

    public Optional<UsersRecord> findByUsername(String username) {
        return dsl.selectFrom(Users.USERS)
            .where(Users.USERS.USERNAME.eq(username))
            .fetchOptional();
    }

    public UsersRecord create(UsersRecord record) {
        dsl.insertInto(Users.USERS)
            .set(record)
            .execute();
        return record;
    }

    public int update(UsersRecord record) {
        return dsl.update(Users.USERS)
            .set(record)
            .where(Users.USERS.ID.eq(record.getId()))
            .execute();
    }

    public int deleteById(UUID id) {
        return dsl.deleteFrom(Users.USERS)
            .where(Users.USERS.ID.eq(id))
            .execute();
    }
}