package com.medilabosolutions.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.service.RiskService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;



@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/risks")
public class RiskController {
    private final RiskService riskService;

    @GetMapping("/diabetes_assessment/patient_id/{patient_id}")
    public ResponseEntity<Mono<AssessmentDto>> getPatientAssessment(@PathVariable(value = "patient_id") Long patientId) {
        return ResponseEntity.ok(riskService.getDiabeteRisk(patientId));
    }

}
