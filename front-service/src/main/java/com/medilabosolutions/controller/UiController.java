package com.medilabosolutions.controller;

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
import org.springframework.web.server.WebSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabosolutions.dto.PatientDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class UiController {

    @Value("${patient.service.url.from.gateway}")
    private String patientServiceUrl;

    @Autowired
    private WebClient webclient;


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

        model.addAttribute("patient", patient);
        return "patient-record";
    }

    @PostMapping("/patient")
    public Mono<Object> createPatient(
            @ModelAttribute(value = "patientToCreate") PatientDto patientToCreate, Model model,
            WebSession session)
            throws JsonProcessingException {

        return webclient.post().uri(patientServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(patientToCreate), PatientDto.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return response.bodyToMono(ProblemDetail.class);
                    } else {
                        return response.bodyToMono(PatientDto.class);
                    }
                })
                .flatMap(body -> {
                    setSessionAttribute(body, session);
                    return Mono.just("redirect:/");
                });

    }

    private void setSessionAttribute(Object body, WebSession session) {

        if (body instanceof ProblemDetail) {
            session.getAttributes().put(ERROR_MESSAGE,
                    "A problem occured : " + ((ProblemDetail) body).getTitle());
        } else {
            session.getAttributes().put(SUCCESS_MESSAGE,
                    "Patient " + ((PatientDto) body).getLastName() + " was correctly created");
        }
    }

    private void addAttributeSessionToModel(Model model, WebSession session, String... messages) {
        for (String message : messages) {
            if (session.getAttribute(message) != null) {
                model.addAttribute(message, session.getAttribute(message));
                session.getAttributes().remove(message);
            }
        }
    }

}
