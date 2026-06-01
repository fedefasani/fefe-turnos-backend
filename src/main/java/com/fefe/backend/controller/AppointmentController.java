package com.fefe.backend.controller;

import com.fefe.backend.dto.AppointmentResponse;
import com.fefe.backend.dto.UpdateAppointmentStatusRequest;
import com.fefe.backend.model.*;
import com.fefe.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final ProfessionalRepository professionalRepository;
    private final BusinessServiceRepository serviceRepository;

    @GetMapping
    public List<AppointmentResponse> getAppointments(@RequestParam(required = false) Long businessId) {
        List<Appointment> appointments;

        if (businessId != null) {
            appointments = appointmentRepository.findByBusinessIdOrderByAppointmentTimeAsc(businessId);
        } else {
            appointments = appointmentRepository.findAll();
        }

        return appointments.stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @GetMapping("/filter")
    public List<AppointmentResponse> getAppointmentsByDate(
            @RequestParam Long businessId,
            @RequestParam String date,
            @RequestParam(required = false) Long professionalId
    ) {
        LocalDateTime start = LocalDateTime.parse(date + "T00:00:00");
        LocalDateTime end = start.plusDays(1);

        List<Appointment> appointments;

        if (professionalId != null) {
            appointments = appointmentRepository.findByBusinessIdAndProfessionalIdAndAppointmentTimeBetween(
                    businessId,
                    professionalId,
                    start,
                    end
            );
        } else {
            appointments = appointmentRepository.findByBusinessIdAndAppointmentTimeBetween(
                    businessId,
                    start,
                    end
            );
        }

        return appointments.stream()
                .map(AppointmentResponse::from)
                .toList();
    }

    @GetMapping("/availability")
    public List<String> getAvailability(
            @RequestParam Long businessId,
            @RequestParam Long serviceId,
            @RequestParam String date,
            @RequestParam Long professionalId
    ) {

        BusinessService service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        if (!service.getBusiness().getId().equals(businessId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El servicio no pertenece al negocio indicado"
            );
        }

        int duration = service.getDurationMinutes();

        LocalDateTime startDay = LocalDateTime.parse(date + "T09:00:00");
        LocalDateTime endDay = LocalDateTime.parse(date + "T18:00:00");

        List<LocalDateTime> allSlots = new java.util.ArrayList<>();

        LocalDateTime current = startDay;
        while (!current.plusMinutes(duration).isAfter(endDay)) {
            allSlots.add(current);
            current = current.plusMinutes(duration);
        }

        List<Appointment> existingAppointments =
                appointmentRepository.findByProfessionalIdAndStatusNotAndAppointmentTimeBetween(
                        professionalId,
                        AppointmentStatus.CANCELLED,
                        startDay,
                        endDay
                );

        List<String> availableSlots = new java.util.ArrayList<>();

        for (LocalDateTime slot : allSlots) {

            LocalDateTime slotEnd = slot.plusMinutes(duration);

            boolean occupied = existingAppointments.stream().anyMatch(existing -> {
                LocalDateTime existingStart = existing.getAppointmentTime();
                LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

                return slot.isBefore(existingEnd) && slotEnd.isAfter(existingStart);
            });

            if (!occupied) {
                availableSlots.add(slot.toLocalTime().toString());
            }
        }

        return availableSlots;
    }

    @PostMapping
    public AppointmentResponse createAppointment(@RequestBody CreateAppointmentRequest request) {

        Business business = businessRepository.findById(request.getBusinessId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Business not found"));

        BusinessService service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        Professional professional = professionalRepository.findById(request.getProfessionalId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Professional not found"));

        Customer customer = customerRepository
                .findByBusinessIdAndPhone(request.getBusinessId(), request.getPhone())
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .business(business)
                            .name(request.getName())
                            .phone(request.getPhone())
                            .email(request.getEmail())
                            .build();

                    return customerRepository.save(newCustomer);
                });

        LocalDateTime requestedStart = request.getAppointmentTime();
        LocalDateTime requestedEnd = requestedStart.plusMinutes(service.getDurationMinutes());

        LocalDateTime dayStart = requestedStart.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<Appointment> existingAppointments =
                appointmentRepository.findByProfessionalIdAndStatusNotAndAppointmentTimeBetween(
                        professional.getId(),
                        AppointmentStatus.CANCELLED,
                        dayStart,
                        dayEnd
                );

        boolean hasOverlap = existingAppointments.stream().anyMatch(existing -> {
            LocalDateTime existingStart = existing.getAppointmentTime();
            LocalDateTime existingEnd = existingStart.plusMinutes(existing.getDurationMinutes());

            return requestedStart.isBefore(existingEnd) && requestedEnd.isAfter(existingStart);
        });

        if (hasOverlap) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El horario seleccionado no está disponible"
            );
        }

        Appointment appointment = Appointment.builder()
                .business(business)
                .customer(customer)
                .professional(professional)
                .service(service)
                .appointmentTime(request.getAppointmentTime())
                .durationMinutes(service.getDurationMinutes())
                .notes(request.getNotes())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);
        return AppointmentResponse.from(savedAppointment);
    }

    @PostMapping("/cancel/{token}")
    public AppointmentResponse cancelAppointment(@PathVariable String token) {

        Appointment appointment = appointmentRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Turno no encontrado"
                ));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El turno ya se encuentra cancelado"
            );
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return AppointmentResponse.from(savedAppointment);
    }

    @PatchMapping("/{id}/status")
    public AppointmentResponse updateAppointmentStatus(
            @PathVariable Long id,
            @RequestBody UpdateAppointmentStatusRequest request
    ) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Turno no encontrado"
                ));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede modificar un turno cancelado"
            );
        }

        appointment.setStatus(request.getStatus());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        return AppointmentResponse.from(savedAppointment);
    }
}