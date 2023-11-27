package com.medilabosolutions.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientDataDto {

    @Min(value = 0, message = "id of patient must be at least greater than 0")
    private Long patient_id;
    
    @NotBlank(message = "name of patient must be not null or blank")
    private String name;
}
