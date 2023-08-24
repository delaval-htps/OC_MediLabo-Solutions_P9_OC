package com.medilabosolutions.controller;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.Patient;
import com.medilabosolutions.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@RequiredArgsConstructor
@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    private final ModelMapper modelMapper;

    /**
     * endpoint to return all patients in db
     * 
     * @return ResponseEntity with reactive stream of all patientDto if existing
     */
    @GetMapping
    public ResponseEntity<Flux<PatientDto>> getAllPatients() {

        return ResponseEntity.ok(patientService.findAll()
                .map(p -> modelMapper.map(p, PatientDto.class)));
    }

    /**
     * endpoint to find a patient given its id
     * 
     * @param patientId id of patient to find
     * @return a reactive stream of ResponseEntity<Patient>:
     *         <ul>
     *         <li>- with HttpStatus.ok and patient in body, if patient was found</li>
     *         <li>- with HttpStatus.notFound with empty patient if was not found</li>
     *         </ul>
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<PatientDto>> getPatientById(@PathVariable("id") Long patientId) {

        return patientService.findById(patientId)
                .map(p -> ResponseEntity.ok(modelMapper.map(p, PatientDto.class)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * create a patient
     * 
     * @param patient in json format in requestBody
     * @return a reactive stream of ResponseEntity with HttpStatus.created and body created patient
     */
    @PostMapping
    public Mono<ResponseEntity<PatientDto>> createPatient(
            @Valid @RequestBody PatientDto patientDto) {

        return patientService.createPatient(modelMapper.map(patientDto, Patient.class))
                .map(p -> new ResponseEntity<PatientDto>(modelMapper.map(p, PatientDto.class),
                        HttpStatus.CREATED));
    }

    /**
     * update field of a existing patient
     * 
     * @param patientId id of patient to update
     * @param patient fields in json format in requestBody
     * @return a reactive stream of ResponseEntity :
     *         <ul>
     *         <li>- with HttpStatus.ok and patient in body, if update operation is ok</li>
     *         <li>- with HttpStatus.badRequest with empty patient if update operation fails</li>
     *         </ul>
     */
    @PutMapping(value = "/{id}")
    public Mono<ResponseEntity<PatientDto>> updatePatient(@PathVariable("id") Long patientId,
            @RequestBody Patient patient) {

        return patientService.updatePatient(patientId, patient)
                .map(p -> ResponseEntity.ok(modelMapper.map(p, PatientDto.class)))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    /**
     * delete a patient by given his id
     * 
     * @param patientId id of patient to delete
     * @return a reactive stream of ResponseEntity :
     *         <ul>
     *         <li>- with HttpStatus.ok and deleted patient in body, if delete operation is ok</li>
     *         <li>- with HttpStatus.notfound with empty patient if delete operation fails because
     *         of not found patient</li>
     *         </ul>
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<PatientDto>> deletePatient(@PathVariable("id") Long patientId) {

        return patientService.deleteById(patientId)
                .map(p -> ResponseEntity.ok(modelMapper.map(p, PatientDto.class)))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
