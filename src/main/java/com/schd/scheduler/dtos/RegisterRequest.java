package com.schd.scheduler.dtos;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String username;
    private String fullName;
}