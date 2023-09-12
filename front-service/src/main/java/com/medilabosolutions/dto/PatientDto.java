package com.medilabosolutions.dto;

import lombok.Data;

@Data
public class PatientDto {
    private String lastName;
    private String firstName;
    private String dateOfBirth;
    private String genre;
    private String address;
    private String phoneNumber;
}

