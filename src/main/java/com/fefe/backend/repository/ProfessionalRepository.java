package com.fefe.backend.repository;

import com.fefe.backend.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalRepository extends JpaRepository<Professional, Long> {

    List<Professional> findByBusinessId(Long businessId);

    List<Professional> findByBusinessIdAndActiveTrue(Long businessId);
}