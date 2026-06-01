package com.fefe.backend.repository;

import com.fefe.backend.model.BusinessService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessServiceRepository extends JpaRepository<BusinessService, Long> {

    List<BusinessService> findByBusinessId(Long businessId);

    List<BusinessService> findByBusinessIdAndActiveTrue(Long businessId);
}