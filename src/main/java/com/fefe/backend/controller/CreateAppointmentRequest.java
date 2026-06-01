package com.fefe.backend.controller;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateAppointmentRequest {

    private Long businessId;
    private Long serviceId;
    private Long professionalId;

    private String name;
    private String phone;
    private String email;

    private LocalDateTime appointmentTime;
    private String notes;
}