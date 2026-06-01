package com.fefe.backend.dto;

import com.fefe.backend.model.Appointment;
import com.fefe.backend.model.AppointmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppointmentResponse {

    private Long id;

    private Long businessId;
    private String businessName;

    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    private Long professionalId;
    private String professionalName;

    private Long serviceId;
    private String serviceName;

    private LocalDateTime appointmentTime;
    private Integer durationMinutes;
    private AppointmentStatus status;
    private String notes;
    private String token;

    public static AppointmentResponse from(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())

                .businessId(appointment.getBusiness().getId())
                .businessName(appointment.getBusiness().getName())

                .customerId(appointment.getCustomer().getId())
                .customerName(appointment.getCustomer().getName())
                .customerPhone(appointment.getCustomer().getPhone())
                .customerEmail(appointment.getCustomer().getEmail())

                .professionalId(appointment.getProfessional().getId())
                .professionalName(appointment.getProfessional().getName())

                .serviceId(appointment.getService().getId())
                .serviceName(appointment.getService().getName())

                .appointmentTime(appointment.getAppointmentTime())
                .durationMinutes(appointment.getDurationMinutes())
                .status(appointment.getStatus())
                .notes(appointment.getNotes())
                .token(appointment.getToken())
                .build();
    }
}