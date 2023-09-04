package com.medilabosolutions.exception;

import java.util.Locale;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalHandlerException  extends ResponseEntityExceptionHandler{

   

    @ExceptionHandler(PatientCreationException.class)
    public String handlePatientCreationExcption(PatientCreationException e,Locale locale){

        //TODO complete exception correctly with all exception from patient-service ( note not use Mono<Object>)
        logger.info(e.getMessage());
        return "error-page";
        //TODO not use error page but redirection on / with error message
    }
}
    

