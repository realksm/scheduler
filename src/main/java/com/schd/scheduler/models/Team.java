package com.schd.scheduler.models;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Team {
 
    private UUID id;
 
    @NotBlank(message = "Team name is required")
    @Size(max = 255)
    private String name;
 
    @NotBlank(message = "Slug is required")
    @Size(min = 2, max = 100)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug may only contain lowercase letters, numbers and hyphens")
    private String slug;
 
    @Size(max = 500)
    private String logoUrl;
 
    @NotNull(message = "Owner is required")
    private UUID ownerId;
 
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
