package com.medilabosolutions.controller;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.service.DiabetesRiskService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/risks")
public class RiskController {

    private final WebClient.Builder lbWebClientBuilder;

    private final DiabetesRiskService riskService;

    @GetMapping("/diabetes_assessment/patient_id/{patient_id}")
    public ResponseEntity<Mono<AssessmentDto>> getPatientAssessment(@PathVariable(value = "patient_id") Long patientId) throws IOException {

        InputStream termTriggersStream = new FileSystemResource(riskService.getTriggerSource()).getInputStream();

        Mono<AssessmentDto> assessmentDto = lbWebClientBuilder.baseUrl("lb://PATIENT-SERVICE").build()
                .get().uri("/patients/{id}", patientId)
                .exchangeToMono(patientResponse -> {

                    if (patientResponse.statusCode().is2xxSuccessful()) {

                        return patientResponse.bodyToMono(PatientDto.class)
                                .flatMap(patient ->

                                lbWebClientBuilder.baseUrl("lb://NOTE-SERVICE").build()
                                        .post().uri("/notes/triggers/patient_id/{id}", patientId)
                                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                        .body(BodyInserters.fromResource(new InputStreamResource(termTriggersStream)))
                                        .exchangeToMono(response ->

                                        response.bodyToMono(Integer.class)
                                                .flatMap(countOfDiabetesTermTriggers ->

                                                Mono.just(riskService.riskAssessment(patient,
                                                        countOfDiabetesTermTriggers))))


                                );
                    }
                    // error the patient is not found or internal error
                    return null;

                });

        return ResponseEntity.ok(assessmentDto);
    }

}
