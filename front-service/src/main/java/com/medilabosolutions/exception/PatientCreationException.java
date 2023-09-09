package com.medilabosolutions.exception;

import org.springframework.web.server.WebSession;
import lombok.Getter;

@Getter
public class PatientCreationException extends RuntimeException{

    // WebSession to use it to redirect attribute to model in redirection
    private final WebSession session;

    public PatientCreationException(String message,WebSession session){
        super(message);
        this.session = session;
    }

  }
