package com.medilabosolutions.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;
    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private String genre;
    private String address;
    private String phoneNumber;
}

