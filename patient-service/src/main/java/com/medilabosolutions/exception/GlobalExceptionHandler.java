package com.medilabosolutions.exception;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

@ControllerAdvice
public class GlobalExceptionHandler  {

    /**
     * BindingResult exception handler
     * 
     * @param exception of type WebExchangeBindException
     * @return a ResponseEntity with list of validation errors message
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<CustomErrorResponse> handleValidationException(
            WebExchangeBindException exception) {

        CustomErrorResponse response = CustomErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST)
                .message("invalid fields")
                .errors(exception.getBindingResult().getAllErrors().stream()
                        .map(errors -> errors.getDefaultMessage())
                        .collect(Collectors.toList()))
                .path(exception.getNestedPath())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

  
   

}
