package com.medilabosolutions.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PatientDataDto {

    @NotNull(message = "id of patient must be not null")
    @Min(value = 0, message = "id of patient must be at least greater than 0")
    private Long id;
    
    @NotBlank(message = "name of patient must be not null or blank")
    private String name;
}
