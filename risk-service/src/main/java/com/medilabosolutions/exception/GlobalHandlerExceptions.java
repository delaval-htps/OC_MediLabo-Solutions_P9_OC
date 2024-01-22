package com.medilabosolutions.exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@PropertySource("classpath:application.properties")
public class GlobalHandlerExceptions {

        @Value("${application.url}")
        private String applicationUrl;

        @Autowired
        private MessageSource messageSource;

        private Logger logger = LogManager.getLogger(GlobalHandlerExceptions.class);

        /**
         * Exception handler for PatientNotFoundException
         * 
         * @param pnfe PatientNotFoundException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(PatientNotFoundException.class)
        public ResponseEntity<ProblemDetail> handlePatientNotFoundException(PatientNotFoundException pnfe, ServerHttpRequest request) throws URISyntaxException {

                ProblemDetail pb = createProblemDetail(HttpStatus.NOT_FOUND,
                                pnfe.getMessage(),
                                messageSource.getMessage("title.not.found", new Object[] {}, Locale.ENGLISH),
                                request);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pb);
        }

        /**
         * Exception handler when RiskServiceException thrown
         * 
         * @param rse riskServiceException thrown
         * @param request the request from where exception was thrown
         * @return a ResponseEntity with custom problem details
         */
        @ExceptionHandler(RiskServiceException.class)
        public ResponseEntity<ProblemDetail> handleRiskServiceException(RiskServiceException rse, ServerHttpRequest request) {

                ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST,
                                rse.getMessage(),
                                messageSource.getMessage("title.error.service", null, Locale.ENGLISH),
                                request);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pb);
        }

        /**
         * Method to create a custom ProblemDetail when a exception is thrown and create a log
         * 
         * @param status statusCode to send
         * @param details the message collected in exception
         * @param title custom title to precise the problem ( see messages.properties)
         * @param request the request from which the exception was thrown to retrieve it's id
         * @return a custom problemDetail with all informations
         */
        private ProblemDetail createProblemDetail(HttpStatus status, String details, String title, ServerHttpRequest request) {

                ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, details);

                try {
                        problemDetail.setType(new URI(applicationUrl));
                } catch (URISyntaxException e) {
                        problemDetail.setProperty("type", applicationUrl);
                }

                problemDetail.setTitle(title);

                problemDetail.setProperty("timeStamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

                problemDetail.setProperty("requestId", request.getId());

                logger.error("{} {} -{}- : {} {}", request.getMethod(),
                                request.getPath(),
                                request.getId(),
                                problemDetail.getTitle(),
                                problemDetail.getDetail());

                return problemDetail;
        }
}
