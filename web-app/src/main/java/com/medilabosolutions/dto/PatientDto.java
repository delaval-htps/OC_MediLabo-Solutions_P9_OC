package com.medilabosolutions.dto;

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
@Builder
@ToString
public class PatientDto {

 
    private Long id;

    private String lastName;

    private String firstName;

    private String dateOfBirth;

    private String genre;

    private String address;

    private String phoneNumber;

}
