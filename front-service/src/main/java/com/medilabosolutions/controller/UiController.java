package com.medilabosolutions.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.WebSession;
import com.medilabosolutions.dto.PatientDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class UiController {

    @Value("${patient.service.url.from.gateway}")
    private String patientServiceUrl;

    @Autowired
    private WebClient webclient;

    private Logger logger = LogManager.getLogger(UiController.class);

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String SUCCESS_MESSAGE = "successMessage";

    /**
     * display the index page of medilabo-solution with the list of all registred PatientDtos
     * 
     * @param model model to add PatientDtos to the view
     * @return the view "index.html"
     */
    @GetMapping("/")
    public String index(Model model, WebSession session) {

        Flux<PatientDto> patients = webclient.get().uri(patientServiceUrl)
                .retrieve().bodyToFlux(PatientDto.class);
        // TODO gestion des erreurs en récupérant le flux de PatientDtos (flux.onErrorResume)

        addAttributeSessionToModel(model, session, ERROR_MESSAGE, SUCCESS_MESSAGE);

        model.addAttribute("patientToCreate", new PatientDto());
        model.addAttribute("patients", patients);
        return "index";
    }

    /**
     * display record of Patient
     * 
     * @param patientId the id of Patient
     * @param model model to return to the view
     * @return the view of the record of Patient (personnal informations for the moment)
     */
    @GetMapping("/patient-record/{id}")
    public String getPatientRecord(@PathVariable(value = "id") Long patientId, Model model) {

        Mono<PatientDto> patient =
                webclient.get().uri(patientServiceUrl + "/{id}", patientId)
                        .retrieve().bodyToMono(PatientDto.class);

        // TODO gestion du retour mono vide

        logger.info("patient with id {} is : {}", patientId, patient.map(Object::toString));

        model.addAttribute("patient", patient);
        return "patient-record";
    }

    /**
     * Create a new patient
     * 
     * @param patientToCreate the patient to create from form of index.html
     * @param model model to add attributes
     * @param session session to add attribute to model when redirect to index.html
     * @return rendering with redirection to index.html
     */
    @PostMapping("/patient")
    public Mono<Rendering> createPatient(
            @ModelAttribute(value = "patientToCreate") PatientDto patientToCreate, Model model,
            WebSession session) {

        return webclient.post().uri(patientServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(patientToCreate), PatientDto.class)

                .exchangeToMono(response -> response.statusCode().isError()
                        ? response.bodyToMono(ProblemDetail.class)
                        : response.bodyToMono(PatientDto.class))

                .flatMap(body -> {
                    setSessionAttribute(body, session, "created !");
                    logger.info("request createPatient return {}", body);
                    return Mono.just(Rendering.redirectTo("/").build());
                });

    }

    @PostMapping("/patient/{id}")
    public Mono<Rendering> updatePatient(@PathVariable(value = "id") Long patientId,
            @ModelAttribute(value = "patient") PatientDto updatedPatient, WebSession session,
            Model model) {

        // TODO bindigResult

        return webclient.put().uri(patientServiceUrl+"/{id}", patientId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedPatient), PatientDto.class)

                .exchangeToMono(response -> response.statusCode().isError()
                        ? response.bodyToMono(ProblemDetail.class)
                        : response.bodyToMono(PatientDto.class)

                ).flatMap(body -> {
                    logger.info("request updatePatient return {}", body);
                    return setSessionAttribute(body, session, "updated").equals(SUCCESS_MESSAGE)
                            ? Mono.just(Rendering.redirectTo("/").build())
                            : Mono.just(Rendering.view("patient-record").build());
                });

    }

    /**
     * method to add an error or success message to websession
     * 
     * @param body body content of a response from webclient request to add to message
     * @param session the websession of the request of application
     * @return String if body is ProblemDetail then return is ERRORMESSAGE else SUCCESSMESSAGE
     */
    private String setSessionAttribute(Object body, WebSession session, String typeOfOperation) {

        if (body instanceof ProblemDetail) {
            session.getAttributes().put(ERROR_MESSAGE,
                    ((ProblemDetail) body).getTitle() + ((ProblemDetail) body).getDetail());
            return ERROR_MESSAGE;
        } else {
            session.getAttributes().put(SUCCESS_MESSAGE,
                    "Patient " + ((PatientDto) body).getLastName() + " was correctly "
                            + typeOfOperation);
            return SUCCESS_MESSAGE;
        }
    }


    /**
     * method to remove attribute of a websession and add it to model of view
     * 
     * @param model the model to add attribute
     * @param session the attribute to remove and add to model of view
     * @param messages list of (String)messages to remove from websession and add to model
     */
    private void addAttributeSessionToModel(Model model, WebSession session, String... messages) {
        for (String message : messages) {
            if (session.getAttribute(message) != null) {
                model.addAttribute(message, session.getAttribute(message));
                session.getAttributes().remove(message);
            }
        }
    }

}
