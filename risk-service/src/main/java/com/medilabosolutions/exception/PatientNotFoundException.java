package com.medilabosolutions.exception;


public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String messageString) {
        super(messageString);
    }
}
