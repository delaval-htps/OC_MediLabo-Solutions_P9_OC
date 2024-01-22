package com.medilabosolutions.service;

import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.model.ExpectedRisk;


public interface RiskAssessmentService {

    public AssessmentDto riskAssessment(PatientDto patient, int countTermTriggers);

    public ExpectedRisk expectedRiskAccordingToGenderAndAge(String genre, int age, int countTermTriggers);

}
