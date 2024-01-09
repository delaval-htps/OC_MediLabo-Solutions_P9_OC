package com.medilabosolutions.controller;

import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.dto.SumTermTriggersDto;
import com.medilabosolutions.exception.PatientNotFoundException;
import com.medilabosolutions.exception.RiskServiceException;
import com.medilabosolutions.service.DiabetesRiskService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/risks")
public class RiskController {

    private final WebClient.Builder lbWebClientBuilder;

    private final DiabetesRiskService riskService;

    private final MessageSource messageSource;

    @GetMapping("/diabetes_assessment/patient_id/{patient_id}")
    public ResponseEntity<Mono<AssessmentDto>> getPatientAssessment(@PathVariable(value = "patient_id") Long patientId) {

        ClassPathResource classPathResource = new ClassPathResource(riskService.getTriggerSource());

        Mono<AssessmentDto> assessmentDto = lbWebClientBuilder.baseUrl("lb://PATIENT-SERVICE").build()
                .get().uri("/patients/{id}", patientId)
                .exchangeToMono(patientResponse -> {

                    if (patientResponse.statusCode().is2xxSuccessful()) {

                        return patientResponse.bodyToMono(PatientDto.class)
                                .flatMap(patient ->

                                lbWebClientBuilder.baseUrl("lb://NOTE-SERVICE").build()
                                        .post().uri("/notes/triggers/patient_id/{id}", patientId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(BodyInserters.fromResource(classPathResource))
                                        .exchangeToMono(response -> {

                                            if (response.statusCode().isError()) {
                                                return response.bodyToMono(ProblemDetail.class)
                                                        .flatMap(pb -> Mono.error(new RiskServiceException(pb.getInstance().toString() + " => " + pb.getTitle() + pb.getDetail())));
                                            }

                                            return response.bodyToMono(SumTermTriggersDto.class)
                                                    .flatMap(sumTermTriggersDto -> Mono.just(riskService.riskAssessment(patient, sumTermTriggersDto.getSumTermTriggers())));
                                        }));
                    }

                    if (patientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new PatientNotFoundException(messageSource.getMessage("patient.not.found", new Object[] {patientId}, Locale.ENGLISH)));
                    }

                    return Mono.error(new RiskServiceException(messageSource.getMessage("error.service", null, Locale.ENGLISH)));
                });

        return ResponseEntity.ok(assessmentDto);
    }

}
