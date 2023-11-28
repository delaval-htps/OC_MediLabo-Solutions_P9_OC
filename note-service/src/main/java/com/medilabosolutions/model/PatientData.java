package com.medilabosolutions.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PatientData {
    private Long patient_id;
    private String name;
    
}
