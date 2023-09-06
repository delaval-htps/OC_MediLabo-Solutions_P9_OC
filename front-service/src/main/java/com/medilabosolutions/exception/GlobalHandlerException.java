package com.medilabosolutions.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalHandlerException  extends ResponseEntityExceptionHandler{

   

    @ExceptionHandler(PatientCreationException.class)
    public String handlePatientCreationExcption(PatientCreationException e){

        //TODO complete exception correctly with all exception from patient-service ( note not use Mono<Object>)
        
        logger.info(e.getMessage());
        e.getSession().getAttributes().put("errorMessage", e.getMessage());
        return "redirect:/";

        
    }
}
    

