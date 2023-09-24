package com.medilabosolutions.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.medilabosolutions.model.Patient;
import com.medilabosolutions.repository.PatientRepository;
import com.medilabosolutions.repository.PatientSortRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientSortRepository patientSortRepository;

    /*
     * method to retrieve all patients in db
     * @return Reactive Stream of all patients presents in db
     */
    public Flux<Patient> findAll() {
        return patientRepository.findAll();
    }

    /**
     * method to return a page of patients
     * 
     * @param pageable abstract interface for pagination
     * @return a Mono of Page of patients
     */
    public Mono<Page<Patient>> findAllByPage(Pageable pageable) {
        return patientSortRepository.findAllBy(pageable)
                .collectList()
                .zipWith(patientRepository.count())
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    /**
     * method to return the patient with id given in param
     * 
     * @param id the given id of patient to find
     * @return Reactive Stream of patient with id given in param
     */
    public Mono<Patient> findById(Long id) {
        return patientRepository.findById(id);
    }

    /**
     * method to create a new patient
     * 
     * @param patient to registre in db
     * @return new reactive stream of new patient just registred in db
     */
    public Mono<Patient> createPatient(Patient patient) {
        return patientRepository.save(patient);
    }

    /**
     * method to update a existing patient defined with patientId given in param.
     * 
     * @param patientId id of patient to update
     * @param patient modified patient retrieve from request body
     * @return reactive stream of patient with modification saved
     */
    public Mono<Patient> updatePatient(Long patientId, Patient patient) {

        return patientRepository.findById(patientId)
                .flatMap(p -> {
                    // use of flatmap instead map 'cause of its return of type
                    // Mono<Patient> contrary to map that is Mono<Object>
                    p.setLastName(patient.getLastName());
                    p.setFirstName(patient.getFirstName());
                    p.setDateOfBirth(patient.getDateOfBirth());
                    p.setGenre(patient.getGenre());
                    p.setAddress(patient.getAddress());
                    p.setPhoneNumber(patient.getPhoneNumber());

                    return patientRepository.save(p);
                });
    }

    /**
     * method to delete a patient found by id given in param
     * 
     * @param patientId id of patient to delete
     * @return reactive stream of deleted patient
     */
    public Mono<Patient> deleteById(Long patientId) {
        return patientRepository.findById(patientId)
                .flatMap(patientToDelete -> patientRepository.delete(patientToDelete)
                        .thenReturn(patientToDelete));
    }
}
