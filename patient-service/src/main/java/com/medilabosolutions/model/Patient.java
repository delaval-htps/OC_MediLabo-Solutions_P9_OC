package com.medilabosolutions.model;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @Column("id")
    private Long id;

    @Column("last_name")
    private String lastName;

    @Column("first_name")
    private String firstName;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    @Column("genre")
    private char genre;

    @Column("patient_address")
    private String address;

    @Column("phone_number")
    private String phoneNumber;
}
