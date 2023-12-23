package com.medilabosolutions.service;

import com.medilabosolutions.dto.AssessmentDto;
import com.medilabosolutions.dto.ExpectedRisk;
import com.medilabosolutions.dto.PatientDto;


public interface RiskAssessmentService {

    public AssessmentDto riskAssessment(PatientDto patient, int countTermTriggers);

    public ExpectedRisk expectedRiskAccordingToGenderAndAge(String genre, int age, int countTermTriggers);

}
