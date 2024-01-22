package com.medilabosolutions.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientDto {

    private Long id;
   
    private String lastName;

    private String firstName;

    private LocalDate dateOfBirth;

    private String genre;

    private String address;

    private String phoneNumber;
}
