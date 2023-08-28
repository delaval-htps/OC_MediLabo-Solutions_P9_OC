package com.medilabosolutions.exception;


public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(Long patientId) {
        super("The patient with id: " + patientId + " was not found");
    }
}
