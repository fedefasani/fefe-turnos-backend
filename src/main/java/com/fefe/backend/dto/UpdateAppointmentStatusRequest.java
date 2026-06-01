package com.fefe.backend.dto;

import com.fefe.backend.model.AppointmentStatus;
import lombok.Data;

@Data
public class UpdateAppointmentStatusRequest {
    private AppointmentStatus status;
}