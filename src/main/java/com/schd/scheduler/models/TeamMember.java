package com.schd.scheduler.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.schd.scheduler.enums.TeamRole;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamMember {
 
    private UUID id;
 
    @NotNull(message = "Team ID is required")
    private UUID teamId;
 
    @NotNull(message = "User ID is required")
    private UUID userId;
 
    @NotNull(message = "Role is required")
    private TeamRole role = TeamRole.MEMBER;
 
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}