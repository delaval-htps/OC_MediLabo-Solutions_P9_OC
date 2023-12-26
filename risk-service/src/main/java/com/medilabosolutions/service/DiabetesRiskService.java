package com.medilabosolutions.service;

import java.time.LocalDate;
import java.time.Period;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.ExpectedRisk;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@Getter
@RequiredArgsConstructor
public class DiabetesRiskService implements RiskAssessmentService {

    @Value("${risk.term.triggers}")
    public String triggerSource;

    public String getTriggerSource(){
        return this.triggerSource;
    }

    @Override
    public AssessmentDto riskAssessment(PatientDto patient, int countTermTriggers) {
        return new AssessmentDto(patient.getId(), expectedRiskAccordingToGenderAndAge(patient.getGenre(),
                patientAge(patient.getDateOfBirth()), countTermTriggers).getAbreviation(), countTermTriggers);
    }

    @Override
    public ExpectedRisk expectedRiskAccordingToGenderAndAge(String genre, int age, int countTermTriggers) {
        if (countTermTriggers == 0) {
            return ExpectedRisk.NONE;
        } else {
            if (age <= 30) {
                if (genre.equals("M")) {
                    return switch (countTermTriggers) {
                        case 1, 2 -> ExpectedRisk.NONE;
                        case 3, 4 -> ExpectedRisk.INDANGER;
                        default -> ExpectedRisk.EARLYONSET;
                    };
                }
                if (genre.equals("F")) {
                    return switch (countTermTriggers) {
                        case 1, 2, 3 -> ExpectedRisk.NONE;
                        case 4, 5, 6 -> ExpectedRisk.INDANGER;
                        default -> ExpectedRisk.EARLYONSET;
                    };
                }
            }
            return switch (countTermTriggers) {
                case 1 -> ExpectedRisk.NONE;
                case 2, 3, 4, 5 -> ExpectedRisk.BORDERLINE;
                case 6, 7 -> ExpectedRisk.INDANGER;
                default -> ExpectedRisk.EARLYONSET;
            };
        }
    }

    private int patientAge(LocalDate patientDateOfBirth) {
        return Period.between(patientDateOfBirth, LocalDate.now()).getYears();
    }
}
