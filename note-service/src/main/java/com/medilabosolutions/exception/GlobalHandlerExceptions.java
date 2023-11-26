package com.medilabosolutions.exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@ControllerAdvice
@PropertySource("classpath:application.properties")
@RequiredArgsConstructor
@Slf4j
public class GlobalHandlerExceptions {

        @Value("${application.url}")
        private String applicationUrl;
     
        private final MessageSource messageSource;

        /**
         * BindingResult exception handler: handle bindigResult , create a custom problemDetail with
         * mapFieldErrors ( HashMap< String, String> fields name as key and default message errors as value)
         * to be able to retrieve fields in error in front service
         * 
         * @param webe of type WebExchangeBindException
         * @return a ResponseEntity with map of fields on error and errors messages
         * @throws URISyntaxException
         */
        @ExceptionHandler(WebExchangeBindException.class)
        public Mono<ResponseEntity<ProblemDetail>> handleValidationException(
                        WebExchangeBindException webe, ServerHttpRequest request) {

                HashMap<String, String> mapFieldErrors = new HashMap<>();

                String fieldsOnError = webe.getBindingResult().getFieldErrors()
                                .stream()
                                .map(error -> {
                                        mapFieldErrors.put(error.getField(), error.getDefaultMessage());
                                        return error.getField();
                                })
                                .collect(Collectors.joining(" , "));

                ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST,
                                fieldsOnError,
                                messageSource.getMessage("title.invalide.fields", new Object[] {}, Locale.ENGLISH),
                                request);

                pb.setProperty("bindingResult", mapFieldErrors);
                return Mono.just(ResponseEntity.badRequest().body(pb));
        }

        /**
         * Exception handler for NoteNotFoundException
         * 
         * @param nnfe NoteNotFoundException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(NoteNotFoundException.class)
        public ResponseEntity<ProblemDetail> handleNoteNotFoundException(
                        NoteNotFoundException nnfe, ServerHttpRequest request)
                        throws URISyntaxException {

                ProblemDetail pb = createProblemDetail(HttpStatus.NOT_FOUND,
                                nnfe.getMessage(),
                                messageSource.getMessage("title.not.found", new Object[] {}, Locale.ENGLISH),
                                request);

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pb);
        }

        /**
         * Exception handler for NoteCreationException when noteDto thrown to service is null (= null
         * entity)
         * 
         * @param nce NoteCreationException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(NoteCreationException.class)
        public ResponseEntity<ProblemDetail> handleNoteCreationException(
                        NoteCreationException nce, ServerHttpRequest request)
                        throws URISyntaxException {

                ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST,
                                nce.getMessage(),
                                messageSource.getMessage("title.not.created.note.null", new Object[] {}, Locale.ENGLISH),
                                request);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pb);
        }

        /**
         * Exception handler for ServerWebInputException in createNote() when data of requestBody not
         * corrects
         * 
         * @param nce ServerWebInputException thrown
         * @return a ResponseEntity with custom problem details
         * @throws URISyntaxException in case that applicationUrl is not type url (not possible)
         */
        @ExceptionHandler(ServerWebInputException.class)
        public ResponseEntity<ProblemDetail> handleException(
                        ServerWebInputException swie, ServerHttpRequest request)
                        throws URISyntaxException {

                ProblemDetail pb = createProblemDetail(HttpStatus.BAD_REQUEST,
                                swie.getMessage(),
                                messageSource.getMessage(
                                                "title.not.created.request.not.correct",
                                                new Object[] {},
                                                Locale.ENGLISH),
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

                log.error("{} {} -{}- : {} {}", request.getMethod(),
                                request.getPath(),
                                request.getId(),
                                problemDetail.getTitle(),
                                problemDetail.getDetail());

                return problemDetail;
        }
}
