package com.medilabosolutions.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.medilabosolutions.model.Patient;
import reactor.core.publisher.Flux;


public interface PatientRepository extends ReactiveCrudRepository<Patient,Long>{
    Flux<Patient> findAllBy(Pageable pageable); 
}
