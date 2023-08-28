package com.medilabosolutions.exception;

public class PatientCreationException extends RuntimeException {


    public PatientCreationException() {
        super("A problem occurs with creation of the new patient");

    }

}
