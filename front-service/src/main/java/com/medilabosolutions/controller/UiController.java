package com.medilabosolutions.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.WebSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medilabosolutions.dto.PatientDto;
import com.medilabosolutions.exception.PatientCreationException;
import com.medilabosolutions.record.Patient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class UiController {

    @Value("${patient.service.url.from.gateway}")
    private String patientServiceUrl;

    @Autowired
    private WebClient webclient;

    /**
     * display the index page of medilabo-solution with the list of all registred PatientDtos
     * 
     * @param model model to add PatientDtos to the view
     * @return the view "index.html"
     */
    @GetMapping("/")
    public String index(Model model, WebSession session) {

        Flux<Patient> patients = webclient.get().uri(patientServiceUrl)
                .retrieve().bodyToFlux(Patient.class);
        // TODO gestion des erreurs en récupérant le flux de PatientDtos (flux.onErrorResume)

        if (session.getAttribute("errorMessage") != null) {
            model.addAttribute("errorMessage", session.getAttribute("errorMessage"));
            session.getAttributes().remove("errorMessage");
        }
        
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

        Mono<Patient> patient =
                webclient.get().uri(patientServiceUrl + "/{id}", patientId)
                        .retrieve().bodyToMono(Patient.class);

        // TODO gestion du retour mono vide

        model.addAttribute("patient", patient);
        return "patient-record";
    }

    @PostMapping("/patient")
    public String createPatient(
            @ModelAttribute(value = "patientToCreate") PatientDto patientToCreate, Model model,
            WebSession session)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        // TODO change mapping with objectmapper directly with object in body


        Mono<Object> createdPatient = webclient.post().uri(patientServiceUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(mapper.writeValueAsString(patientToCreate)))
                .retrieve().bodyToMono(Object.class)
                .onErrorMap(WebClientResponseException.class,
                        e -> new PatientCreationException(e.getResponseBodyAsString(), session));


        model.addAttribute("createdPatient", createdPatient);
        return "redirect:/";
    }
}
