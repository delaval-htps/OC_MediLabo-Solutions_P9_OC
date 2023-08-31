package com.medilabosolutions.record;

import java.time.LocalDate;

public record Patient(
                Long id,
                String lastName,
                String firstName,
                LocalDate dateOfBirth,
                String genre,
                String address,
                String phoneNumber) {
}

