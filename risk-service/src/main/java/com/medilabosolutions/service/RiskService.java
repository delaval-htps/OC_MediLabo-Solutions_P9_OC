package com.medilabosolutions.service;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.ExpectedRisk;
import com.medilabosolutions.dto.PatientDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RiskService {

    private final WebClient.Builder lbWebClientBuilder;


    public Mono<AssessmentDto> getDiabeteRisk(Long patientId) {

        return lbWebClientBuilder.baseUrl("lb://PATIENT-SERVICE").build()
                .get().uri("/patients/{id}", patientId)
                .exchangeToMono(patientResponse -> {

                    if (patientResponse.statusCode().is2xxSuccessful()) {

                        return patientResponse.bodyToMono(PatientDto.class)
                                .flatMap(patient -> {

                                    // send a request to note-service to calculate the count of term triggers for diabete in all notes
                                    // of patient using mongodb query
                                    return lbWebClientBuilder.baseUrl("lb://NOTE-SERVICE").build()
                                            .get().uri("/notes/diabetes_triggers/patient_id/{id}", patientId)
                                            .exchangeToMono(response -> {

                                                return response.bodyToMono(Integer.class)
                                                        .flatMap(countOfDiabetesTermTriggers -> {

                                                            // diabetes assessment for patient
                                                            return Mono.just(diabetesAssessment(patientId,
                                                                    patient.getGenre(),
                                                                    patient.getDateOfBirth(),
                                                                    countOfDiabetesTermTriggers));
                                                        });
                                            });

                                });
                    }
                    // error the patient is not found or internal error
                    return null;

                });

    }

    private AssessmentDto diabetesAssessment(Long patientId, String patientGenre, LocalDate patientDateOfBirth, int countTermTriggers) {

        if (countTermTriggers > 0) {

            if (patientGenre.equals("M")) {
                return new AssessmentDto(patientId, defineExpectedRiskForMan(countTermTriggers).getAbreviation());
            }

            if (patientGenre.equals("F")) {
                return new AssessmentDto(patientId, defineExpectedRiskForWoman(countTermTriggers).getAbreviation());
            }
        }

        return new AssessmentDto(patientId, ExpectedRisk.NONE.getAbreviation());
    }


    private ExpectedRisk defineExpectedRiskForMan(int countTermTriggers) {
        return ExpectedRisk.BORDERLINE;
    }

    private ExpectedRisk defineExpectedRiskForWoman(int countTermTriggers) {
        return ExpectedRisk.INDANGER;
    }
}
