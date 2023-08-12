package com.medilabosolutions.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.medilabosolutions.model.Patient;


public interface PatientRepository extends ReactiveCrudRepository<Patient,Long>{
    
}
