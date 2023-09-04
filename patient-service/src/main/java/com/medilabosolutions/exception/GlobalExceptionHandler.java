package com.medilabosolutions.exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@ControllerAdvice
@PropertySource("classpath:application.properties")
public class GlobalExceptionHandler {

        @Value("${application.url}")
        private String applicationUrl;

        private static final String INVALIDFIELDS = "Invalid fields in Patient";
        private static final String PATIENTNOTFOUND = "Patient Not Found";
        private static final String PATIENTNOTCREATED = "Patient Not Created";

        /**
         * BindingResult exception handler
         * 
         * @param webe of type WebExchangeBindException
         * @return a ResponseEntity with list of validation errors message
         * @throws URISyntaxException
         */
        @ExceptionHandler(WebExchangeBindException.class)
        public Mono<ResponseEntity<ProblemDetail>> handleValidationException(
                        WebExchangeBindException webe, ServerHttpRequest request) {

                String bindingResult = webe.getBindingResult().getAllErrors().stream()
                                .map(errors -> errors.getDefaultMessage())
                                .collect(Collectors.toList()).toString();

                return Mono.just(ResponseEntity.badRequest()
                                .body(createProblemDetail(HttpStatus.BAD_REQUEST,
                                                bindingResult, INVALIDFIELDS, request)));
        }

        /**
         * Exception handler for PatientNotFoundException
         * 
         * @param pnfe PatientNotFoundException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(PatientNotFoundException.class)
        public ResponseEntity<ProblemDetail> handlePatientNotFoundException(
                        PatientNotFoundException pnfe, ServerHttpRequest request)
                        throws URISyntaxException {

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createProblemDetail(
                                HttpStatus.NOT_FOUND, pnfe.getMessage(), PATIENTNOTFOUND, request));
        }

        /**
         * Exception handler for PatientCreationException when patientDto thrown to service is null
         * 
         * @param pce PatientCreationException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(PatientCreationException.class)
        public ResponseEntity<ProblemDetail> handlePatientCreationException(
                        PatientCreationException pce, ServerHttpRequest request)
                        throws URISyntaxException {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(createProblemDetail(HttpStatus.BAD_REQUEST, pce.getMessage(),
                                                PATIENTNOTCREATED, request));
        }

        /**
         * Exception handler for ServerWebInputException in createPatient() when data of requestBody
         * not corrects
         * 
         * @param pce ServerWebInputException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(ServerWebInputException.class)
        public ResponseEntity<ProblemDetail> handleException(
                        ServerWebInputException swie, ServerHttpRequest request)
                        throws URISyntaxException {

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(createProblemDetail(HttpStatus.BAD_REQUEST, swie.getMessage(),
                                                PATIENTNOTCREATED, request));
        }


        private ProblemDetail createProblemDetail(HttpStatus status, String details, String title,
                        ServerHttpRequest request) {

                ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, details);
                try {
                        problemDetail.setType(new URI(applicationUrl));
                } catch (URISyntaxException e) {
                        problemDetail.setProperty("type", applicationUrl);
                }
                problemDetail.setTitle(title);
                problemDetail.setProperty("timeStamp",
                                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                problemDetail.setProperty("requestId", request.getId());

                return problemDetail;
        }
}
