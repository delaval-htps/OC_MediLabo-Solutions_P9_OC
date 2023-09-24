package com.medilabosolutions.repository;

import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import com.medilabosolutions.model.Patient;
import reactor.core.publisher.Flux;

public interface PatientSortRepository extends ReactiveSortingRepository<Patient, Long> {
    Flux<Patient> findAllBy(org.springframework.data.domain.Pageable pageable);
}
