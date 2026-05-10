package com.schd.scheduler.dtos;

import lombok.Data;

@Data
public class UserRequest {
    private String fullName;
    private String avatarUrl;
    private String timezone;
    private String locale;
}