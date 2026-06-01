package com.fefe.backend.repository;

import com.fefe.backend.model.Appointment;
import com.fefe.backend.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByBusinessId(Long businessId);

    List<Appointment> findByBusinessIdOrderByAppointmentTimeAsc(Long businessId);

    List<Appointment> findByBusinessIdAndAppointmentTimeBetween(
            Long businessId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByBusinessIdAndProfessionalIdAndAppointmentTimeBetween(
            Long businessId,
            Long professionalId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByProfessionalIdAndAppointmentTimeBetween(
            Long professionalId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Appointment> findByProfessionalIdAndStatusNotAndAppointmentTimeBetween(
            Long professionalId,
            AppointmentStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Appointment> findByToken(String token);

    List<Appointment> findByBusinessIdAndStatus(Long businessId, AppointmentStatus status);
}